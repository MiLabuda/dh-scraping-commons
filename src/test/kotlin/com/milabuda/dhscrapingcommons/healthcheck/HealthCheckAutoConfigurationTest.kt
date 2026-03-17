package com.milabuda.dhscrapingcommons.healthcheck

import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import com.milabuda.dhscrapingcommons.util.UtilAutoConfiguration
import io.github.resilience4j.retry.RetryRegistry
import io.micrometer.observation.ObservationRegistry
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

class HealthCheckAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(UtilAutoConfiguration::class.java, HealthCheckAutoConfiguration::class.java))
        .withUserConfiguration(WebClientBuilderConfig::class.java)
        .withPropertyValues("scraping.health.portal-url=http://localhost:8080")

    // -------------------------------------------------------------------------
    // Bean registration — happy path
    // -------------------------------------------------------------------------

    @Test
    fun `PortalHealthChecker bean is registered by default`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(PortalHealthChecker::class.java)
        }
    }

    // -------------------------------------------------------------------------
    // @ConditionalOnMissingBean — user-provided bean is NOT overridden
    // -------------------------------------------------------------------------

    @Test
    fun `user-provided PortalHealthChecker bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomPortalHealthCheckerConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(PortalHealthChecker::class.java)
                val checker = ctx.getBean(PortalHealthChecker::class.java)
                assertThat(checker).isSameAs(CustomPortalHealthCheckerConfig.INSTANCE)
            }
    }

    // -------------------------------------------------------------------------
    // PortalHealthProperties binding
    // -------------------------------------------------------------------------

    @Test
    fun `PortalHealthProperties are bound from application properties`() {
        contextRunner
            .withPropertyValues(
                "scraping.health.portal-url=https://example.com",
                "scraping.health.retry.max-attempts=5",
                "scraping.health.cache.ttl=PT30S",
            )
            .run { ctx ->
                val props = ctx.getBean(PortalHealthProperties::class.java)
                assertThat(props.portalUrl).isEqualTo("https://example.com")
                assertThat(props.retry.maxAttempts).isEqualTo(5)
                assertThat(props.cache.ttl.seconds).isEqualTo(30)
            }
    }

    // -------------------------------------------------------------------------
    // Missing required property — context should fail to start
    // -------------------------------------------------------------------------

    @Test
    fun `context fails to start when required portal-url property is missing`() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(UtilAutoConfiguration::class.java, HealthCheckAutoConfiguration::class.java))
            .withUserConfiguration(WebClientBuilderConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasFailed()
            }
    }

    // -------------------------------------------------------------------------
    // Shared test configurations
    // -------------------------------------------------------------------------

    @Configuration
    class WebClientBuilderConfig {
        @Bean
        fun webClientBuilder(): WebClient.Builder = WebClient.builder()

        @Bean
        fun retryRegistry(): RetryRegistry = mockk(relaxed = true)

        @Bean
        fun observationRegistry(): ObservationRegistry = mockk(relaxed = true)
    }

    @Configuration
    class CustomPortalHealthCheckerConfig {
        companion object {
            val INSTANCE: PortalHealthChecker = mockk()
        }

        @Bean
        fun portalHealthChecker(): PortalHealthChecker = INSTANCE
    }
}
