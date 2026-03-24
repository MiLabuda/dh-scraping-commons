package com.milabuda.dhscrapingcommons.imagearchive

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.annotation.Observed
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import software.amazon.awssdk.services.s3.model.PutObjectRequest

private const val ARCHIVE_IMAGES_TIMEOUT = 10_000L
private val log = KotlinLogging.logger {}

private fun determineContentType(photoUrl: String): String =
    when (photoUrl.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }

open class S3ImageArchiver(
    private val s3props: S3ImageBucketProperties,
    private val keyBuilder: S3ImageKeyBuilder,
    private val imageDownloader: ImageDownloaderPort,
    private val s3Mediator: S3MediatorPort,
    private val networkThrottle: Semaphore,
    private val meterRegistry: MeterRegistry,
    private val observationRegistry: ObservationRegistry,
) : ImageArchiver {

    @PostConstruct
    fun registerMetrics() {
        Gauge.builder("network.throttle.available", networkThrottle) {
            it.availablePermits.toDouble()
        }
            .tag("component", "S3ImageArchiver")
            .description("Number of available slots in the network throttle semaphore")
            .register(meterRegistry)
    }

    @Observed(
        name = "property.post.collection.image.archival",
        contextualName = "Archiving property post images to S3",
        lowCardinalityKeyValues = ["operation", "collectPosts"],
    )
    override fun saveAll(propertyId: String, imageUrls: List<PropertyImage>): List<ImageUploadResult> {
        // Capture the active observation (set by @Observed AOP) before entering coroutines.
        // Coroutines switch threads (Dispatchers.IO), so the ThreadLocal-based observation
        // context is lost. We restore it manually via openScope() in each coroutine.
        val parentObservation = observationRegistry.currentObservation

        val results = runBlocking {
            runCatching {
                withTimeout(ARCHIVE_IMAGES_TIMEOUT) {
                    withContext(Dispatchers.IO) {
                        supervisorScope {
                            imageUrls.mapIndexed { index, image ->
                                async {
                                    saveSingleImage(propertyId, image.imageUrl, index, parentObservation)
                                }
                            }.awaitAll()
                                .filterNotNull()
                        }
                    }
                }
            }.onFailure { e ->
                when (e) {
                    is TimeoutCancellationException ->
                        log.error { "Timed out waiting for image upload for propertyId: $propertyId" }
                    else ->
                        log.error(e) { "Unexpected error during image archival for propertyId: $propertyId" }
                }
            }
                .getOrNull()
        }

        return when {
            results.isNullOrEmpty() -> {
                log.warn { "No images archived for property: $propertyId" }
                emptyList()
            }
            else -> {
                log.info { "Successfully archived ${results.size}/${imageUrls.size} images for property: $propertyId" }
                results
            }
        }
    }

    private suspend fun saveSingleImage(
        propertyId: String,
        photoUrl: String,
        sequenceNumber: Int,
        parentObservation: io.micrometer.observation.Observation?,
    ): ImageUploadResult? = networkThrottle.withPermit {
        // Restore parent observation in this IO thread so that child observations in
        // ImageDownloader and S3Mediator produce child spans instead of root spans.
        val scope = parentObservation?.openScope()
        try {
            log.debug { "Uploading image $sequenceNumber for property $propertyId: $photoUrl" }

            val downloadResult = imageDownloader.downloadImage(photoUrl)
                .getOrElse {
                    log.debug { "Failed to download image: [$photoUrl]" }
                    return@withPermit null
                }

            runCatching {
                uploadToS3(propertyId, photoUrl, sequenceNumber, downloadResult)
            }.onFailure { e ->
                log.warn(e) { "Failed to upload image to S3 property id: [$propertyId] url: [$photoUrl], skipping" }
            }.getOrNull()
        } finally {
            scope?.close()
        }
    }

    private suspend fun uploadToS3(
        propertyId: String,
        photoUrl: String,
        sequenceNumber: Int,
        downloadResult: DownloadResult,
    ): ImageUploadResult {
        downloadResult.inputStream.use { imageData ->
            val key = keyBuilder.buildKey(propertyId, photoUrl, sequenceNumber)
            val putRequest = buildRequest(key, photoUrl, downloadResult)

            s3Mediator.delegateUploadToS3(putRequest, imageData, downloadResult.contentLength)
            log.debug { "Successfully uploaded image to S3: [$photoUrl] -> [$key]" }

            return ImageUploadResult(photoUrl, key)
        }
    }

    private fun buildRequest(key: String, photoUrl: String, downloadResult: DownloadResult): PutObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3props.name)
            .key(key)
            .contentType(determineContentType(photoUrl))
            .contentLength(downloadResult.contentLength)
            .build()
}
