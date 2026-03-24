package com.milabuda.dhscrapingcommons.imagearchive

import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private const val DOWNLOAD_TIMEOUT_SECONDS = 5L
private const val BUFFER_SIZE = 128 * 1024
private val log = KotlinLogging.logger {}

private fun HttpResponse<InputStream>.validateContentLength(url: String): Long =
    headers()
        .firstValueAsLong("Content-Length")
        .takeIf { it.isPresent && it.asLong > 0 }
        ?.asLong
        ?: throw ImageDownloadException.MissingContentLength(url)

open class ImageDownloader(
    private val client: HttpClient,
    private val userAgentProvider: UserAgentProvider,
    private val observationRegistry: ObservationRegistry,
) {

    fun downloadImage(photoUrl: String): Result<DownloadResult> {
        val observation = Observation.createNotStarted("property.post.collection.image.download", observationRegistry)
            .contextualName("Downloading property post images from origin URL")
            .lowCardinalityKeyValue("operation", "downloadPosts")

        return observation.observe<Result<DownloadResult>> {
            runCatching {
                val request = buildRequest(photoUrl)
                val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
                handleResponse(response, photoUrl)
            }.onFailure { e ->
                log.error(e) { "Failed to download image from URL [$photoUrl]" }
            }
        } ?: Result.failure(IllegalStateException("Observation returned null for $photoUrl"))
    }

    private fun handleResponse(response: HttpResponse<InputStream>, url: String): DownloadResult =
        when (response.statusCode()) {
            200 -> createDownloadResult(response, url)
            404 -> throw ImageDownloadException.NotFound(url)
            in 400..499 -> throw ImageDownloadException.ClientError(url, response.statusCode())
            in 500..599 -> throw ImageDownloadException.ServerError(url, response.statusCode())
            else -> throw ImageDownloadException.UnexpectedStatus(url, response.statusCode())
        }

    private fun createDownloadResult(response: HttpResponse<InputStream>, url: String): DownloadResult {
        val contentLength = response.validateContentLength(url)
        val bufferedStream = BufferedInputStream(response.body(), BUFFER_SIZE)

        return DownloadResult(bufferedStream, contentLength)
    }

    private fun buildRequest(photoUrl: String): HttpRequest =
        HttpRequest.newBuilder(URI.create(photoUrl))
            .timeout(Duration.ofSeconds(DOWNLOAD_TIMEOUT_SECONDS))
            .header("User-Agent", userAgentProvider.provide())
            .GET()
            .build()
}
