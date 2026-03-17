package com.milabuda.dhscrapingcommons.healthcheck

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import com.milabuda.dhscrapingcommons.util.BROWSER_HEADERS
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

private val log = KotlinLogging.logger {}

class PortalHealthChecker(
    private val webClientBuilder: WebClient.Builder,
    private val userAgentProvider: UserAgentProvider,
    private val properties: PortalHealthProperties
) {

    private val webClient: WebClient by lazy { webClientBuilder.build() }

    private val retry: Retry by lazy {
        val config = RetryConfig.custom<Boolean>()
            .maxAttempts(properties.retry.maxAttempts)
            .waitDuration(properties.retry.waitDuration)
            .retryOnResult { result -> !result }
            .apply {
                if (!properties.retry.ignoreExceptions) {
                    retryOnException { true }
                }
            }
            .build()

        Retry.of("portalHealthChecker", config).also { retry ->
            retry.eventPublisher.onRetry { event ->
                log.warn { "Health check retry #${event.numberOfRetryAttempts} for [${properties.portalUrl}]" }
            }
            retry.eventPublisher.onError { event ->
                log.error { "Health check failed after ${event.numberOfRetryAttempts} attempts for [${properties.portalUrl}]" }
            }
        }
    }

    private val cache: Cache<String, Boolean> by lazy {
        Caffeine.newBuilder()
            .expireAfterWrite(properties.cache.ttl)
            .maximumSize(properties.cache.maximumSize)
            .build()
    }

    fun checkPortalStatus(): Boolean =
        cache.get("status") {
            Retry.decorateSupplier(retry) { doCheck() }
                .runCatching { get() }
                .getOrElse { e ->
                    log.error(e) { "Health check error for [${properties.portalUrl}]" }
                    false
                }
        }!!

    private fun doCheck(): Boolean {
        val statusCode = webClient
            .get()
            .uri(properties.portalUrl)
            .header(HttpHeaders.USER_AGENT, userAgentProvider.provide())
            .headers { h -> BROWSER_HEADERS.forEach { (k, v) -> h.set(k, v) } }
            .exchangeToMono { response -> Mono.just(response.statusCode()) }
            .block(Duration.ofSeconds(5))

        val isUp = statusCode?.is2xxSuccessful ?: false
        if (!isUp) {
            log.warn { "Health check for [${properties.portalUrl}] returned HTTP status: $statusCode" }
        }
        return isUp
    }
}
