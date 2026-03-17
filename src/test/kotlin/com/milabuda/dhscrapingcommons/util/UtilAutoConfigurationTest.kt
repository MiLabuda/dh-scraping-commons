package com.milabuda.dhscrapingcommons.util

import com.milabuda.dhscrapingcommons.healthcheck.HealthCheckAutoConfiguration
import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
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

class UtilAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(UtilAutoConfiguration::class.java))

    // -------------------------------------------------------------------------
    // Bean registration — happy path
    // -------------------------------------------------------------------------

    @Test
    fun `UserAgentProvider bean is registered by default`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(UserAgentProvider::class.java)
        }
    }

    // -------------------------------------------------------------------------
    // @ConditionalOnMissingBean — user-provided bean is NOT overridden
    // -------------------------------------------------------------------------

    @Test
    fun `user-provided UserAgentProvider bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomUserAgentProviderConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(UserAgentProvider::class.java)
                val provider = ctx.getBean(UserAgentProvider::class.java)
                assertThat(provider).isSameAs(CustomUserAgentProviderConfig.INSTANCE)
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
    class CustomUserAgentProviderConfig {
        companion object {
            val INSTANCE: UserAgentProvider = mockk()
        }

        @Bean
        fun userAgentProvider(): UserAgentProvider = INSTANCE
    }
}
