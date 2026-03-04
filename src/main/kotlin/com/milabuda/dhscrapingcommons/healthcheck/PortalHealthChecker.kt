package com.milabuda.dhscrapingcommons.healthcheck

import com.milabuda.dhscrapingcommons.util.BROWSER_HEADERS
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

private val log = KotlinLogging.logger {}

@Component
class PortalHealthChecker(
    private val webClientBuilder: WebClient.Builder,
    private val userAgentProvider: UserAgentProvider,
    @Value("\${scraping.health.portal-url}") private val portalUrl: String
) {

    private val webClient: WebClient by lazy { webClientBuilder.build() }

    fun checkPortalStatus(): Boolean {
        val statusCode = webClient
            .get()
            .uri(portalUrl)
            .header(HttpHeaders.USER_AGENT, userAgentProvider.provide())
            .headers { h -> BROWSER_HEADERS.forEach { (k, v) -> h.set(k, v) } }
            .exchangeToMono { response -> Mono.just(response.statusCode()) }
            .block(Duration.ofSeconds(5))

        val isUp = statusCode?.is2xxSuccessful ?: false
        if (!isUp) {
            log.warn { "Health check for [$portalUrl] returned HTTP status: $statusCode" }
        }
        return isUp
    }
}
