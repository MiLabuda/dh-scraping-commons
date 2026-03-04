package com.milabuda.dhscrapingcommons.healthcheck

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "scraping.health")
data class PortalHealthProperties(
    val portalUrl: String,
    val retry: RetryProperties = RetryProperties(),
    val cache: CacheProperties = CacheProperties()
) {
    data class RetryProperties(
        val maxAttempts: Int = 3,
        val waitDuration: Duration = Duration.ofSeconds(2),
        val ignoreExceptions: Boolean = false
    )

    data class CacheProperties(
        val ttl: Duration = Duration.ofMinutes(1),
        val maximumSize: Long = 10
    )
}
