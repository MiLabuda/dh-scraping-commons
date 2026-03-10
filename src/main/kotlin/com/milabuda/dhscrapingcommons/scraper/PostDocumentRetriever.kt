package com.milabuda.dhscrapingcommons.scraper

import com.milabuda.dhscrapingcommons.util.BROWSER_HEADERS
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private val log = KotlinLogging.logger {}

private const val JSOUP_TIMEOUT_MS = 10_000
private const val RETRY_NAME = "documentServiceRetry"
private const val OBSERVATION_NAME = "property.post.collection.retrieval"
private const val CONTEXTUAL_NAME = "Retrieving property post document"

class PostDocumentRetriever(
    private val userAgentProvider: UserAgentProvider,
    private val retryRegistry: RetryRegistry,
    private val observationRegistry: ObservationRegistry,
) {
    fun retrieve(url: String): Document {
        val retry = retryRegistry.retry(RETRY_NAME)

        return Observation.createNotStarted(OBSERVATION_NAME, observationRegistry)
            .contextualName(CONTEXTUAL_NAME)
            .lowCardinalityKeyValue("operation", "collectPosts")
            .observe<Document> {
                Retry.decorateCheckedSupplier(retry) {
                    Jsoup.connect(url)
                        .userAgent(userAgentProvider.provide())
                        .headers(BROWSER_HEADERS)
                        .timeout(JSOUP_TIMEOUT_MS)
                        .get()
                }.get()
            } ?: error("Observation returned null for URL: $url")
    }
}
