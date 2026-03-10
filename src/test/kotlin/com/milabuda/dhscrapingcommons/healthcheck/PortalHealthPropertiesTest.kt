package com.milabuda.dhscrapingcommons.healthcheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class PortalHealthPropertiesTest {

    @Test
    fun `RetryProperties has correct default values`() {
        val retry = PortalHealthProperties.RetryProperties()

        assertThat(retry.maxAttempts).isEqualTo(3)
        assertThat(retry.waitDuration).isEqualTo(Duration.ofSeconds(2))
        assertThat(retry.ignoreExceptions).isFalse()
    }

    @Test
    fun `CacheProperties has correct default values`() {
        val cache = PortalHealthProperties.CacheProperties()

        assertThat(cache.ttl).isEqualTo(Duration.ofMinutes(1))
        assertThat(cache.maximumSize).isEqualTo(10L)
    }

    @Test
    fun `PortalHealthProperties uses default nested properties when not specified`() {
        val props = PortalHealthProperties(portalUrl = "https://example.com")

        assertThat(props.portalUrl).isEqualTo("https://example.com")
        assertThat(props.retry).isEqualTo(PortalHealthProperties.RetryProperties())
        assertThat(props.cache).isEqualTo(PortalHealthProperties.CacheProperties())
    }

    @Test
    fun `PortalHealthProperties accepts custom retry configuration`() {
        val customRetry = PortalHealthProperties.RetryProperties(
            maxAttempts = 5,
            waitDuration = Duration.ofSeconds(10),
            ignoreExceptions = true,
        )
        val props = PortalHealthProperties(portalUrl = "https://example.com", retry = customRetry)

        assertThat(props.retry.maxAttempts).isEqualTo(5)
        assertThat(props.retry.waitDuration).isEqualTo(Duration.ofSeconds(10))
        assertThat(props.retry.ignoreExceptions).isTrue()
    }

    @Test
    fun `PortalHealthProperties accepts custom cache configuration`() {
        val customCache = PortalHealthProperties.CacheProperties(
            ttl = Duration.ofMinutes(5),
            maximumSize = 100L,
        )
        val props = PortalHealthProperties(portalUrl = "https://example.com", cache = customCache)

        assertThat(props.cache.ttl).isEqualTo(Duration.ofMinutes(5))
        assertThat(props.cache.maximumSize).isEqualTo(100L)
    }
}
