package com.milabuda.dhscrapingcommons.healthcheck

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.unauthorized
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_ACCEPT
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_ACCEPT_LANGUAGE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_CACHE_CONTROL
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_DEST
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_MODE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_SITE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_USER
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_UPGRADE_INSECURE_REQUESTS
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

class PortalHealthCheckerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort())
            .build()
    }

    private lateinit var healthChecker: PortalHealthChecker

    @BeforeEach
    fun setUp() {
        healthChecker = buildChecker(wireMock.baseUrl())
    }

    private fun buildChecker(
        portalUrl: String,
        maxAttempts: Int = 3,
    ): PortalHealthChecker {
        val properties = PortalHealthProperties(
            portalUrl = portalUrl,
            retry = PortalHealthProperties.RetryProperties(
                maxAttempts = maxAttempts,
                waitDuration = Duration.ofMillis(10),
                ignoreExceptions = false,
            ),
            cache = PortalHealthProperties.CacheProperties(
                ttl = Duration.ofMillis(100),
                maximumSize = 10,
            ),
        )
        val userAgentProvider = mockk<UserAgentProvider>().also {
            every { it.provide() } returns "Mozilla/5.0 (Test) TestBrowser/1.0"
        }
        return PortalHealthChecker(WebClient.builder(), userAgentProvider, properties)
    }

    @Test
    fun `checkPortalStatus returns true when portal responds with 200`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(ok()))

        val result = healthChecker.checkPortalStatus()

        assertThat(result).isTrue()
    }

    @Test
    fun `checkPortalStatus returns false when portal responds with 401`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(unauthorized()))

        val result = healthChecker.checkPortalStatus()

        assertThat(result).isFalse()
    }

    @Test
    fun `checkPortalStatus returns false when portal responds with 500`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(serverError()))

        val result = healthChecker.checkPortalStatus()

        assertThat(result).isFalse()
    }

    @Test
    fun `checkPortalStatus caches result - HTTP called only once on two invocations`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(ok()))

        healthChecker.checkPortalStatus()
        healthChecker.checkPortalStatus()

        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/")))
    }

    @Test
    fun `checkPortalStatus retries maxAttempts times when portal returns non-2xx`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(serverError()))

        healthChecker.checkPortalStatus()

        // Resilience4j retry with maxAttempts=3 means 3 total attempts (1 initial + 2 retries)
        wireMock.verify(3, getRequestedFor(urlPathEqualTo("/")))
    }

    @Test
    fun `checkPortalStatus sends User-Agent header`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(ok()))

        healthChecker.checkPortalStatus()

        wireMock.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withHeader(
                    HttpHeaders.USER_AGENT,
                    com.github.tomakehurst.wiremock.matching.EqualToPattern("Mozilla/5.0 (Test) TestBrowser/1.0"),
                )
        )
    }

    @Test
    fun `checkPortalStatus sends all BROWSER_HEADERS`() {
        wireMock.stubFor(get(urlPathEqualTo("/")).willReturn(ok()))

        healthChecker.checkPortalStatus()

        wireMock.verify(
            getRequestedFor(urlPathEqualTo("/"))
                .withHeader(HEADER_ACCEPT, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_ACCEPT_LANGUAGE, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_UPGRADE_INSECURE_REQUESTS, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_SEC_FETCH_DEST, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_SEC_FETCH_MODE, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_SEC_FETCH_SITE, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_SEC_FETCH_USER, com.github.tomakehurst.wiremock.matching.AnythingPattern())
                .withHeader(HEADER_CACHE_CONTROL, com.github.tomakehurst.wiremock.matching.AnythingPattern())
        )
    }

    @Test
    fun `checkPortalStatus returns false when connection is refused`() {
        val checker = buildChecker(portalUrl = "http://localhost:1", maxAttempts = 1)

        val result = checker.checkPortalStatus()

        assertThat(result).isFalse()
    }
}
