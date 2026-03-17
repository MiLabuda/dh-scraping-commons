package com.milabuda.dhscrapingcommons.runner

import com.milabuda.dhscrapingcommons.healthcheck.HealthCheckAutoConfiguration
import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
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

class JobRunnerAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                UtilAutoConfiguration::class.java,
                HealthCheckAutoConfiguration::class.java,
                JobRunnerAutoConfiguration::class.java,
            )
        )
        .withUserConfiguration(WebClientBuilderConfig::class.java)
        .withPropertyValues("scraping.health.portal-url=http://localhost:8080")

    // -------------------------------------------------------------------------
    // Bean registration — happy path
    // -------------------------------------------------------------------------

    @Test
    fun `JobRunnerSupport bean is registered by default`() {
        contextRunner
            .withPropertyValues("spring.application.name=test-scraper")
            .run { ctx ->
                assertThat(ctx).hasSingleBean(JobRunnerSupport::class.java)
            }
    }

    @Test
    fun `JobRunner bean is registered when CollectIdsPort and CollectPostsPort are present`() {
        contextRunner
            .withUserConfiguration(MockPortsConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(JobRunner::class.java)
            }
    }

    @Test
    fun `JobRunner bean is NOT registered when ports are absent`() {
        contextRunner.run { ctx ->
            assertThat(ctx).doesNotHaveBean(JobRunner::class.java)
        }
    }

    // -------------------------------------------------------------------------
    // @ConditionalOnMissingBean — user-provided beans are NOT overridden
    // -------------------------------------------------------------------------

    @Test
    fun `user-provided JobRunnerSupport bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomJobRunnerSupportConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(JobRunnerSupport::class.java)
                val runner = ctx.getBean(JobRunnerSupport::class.java)
                assertThat(runner).isSameAs(CustomJobRunnerSupportConfig.INSTANCE)
            }
    }

    @Test
    fun `user-provided JobRunner bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(MockPortsConfig::class.java, CustomJobRunnerConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(JobRunner::class.java)
                val runner = ctx.getBean(JobRunner::class.java)
                assertThat(runner).isSameAs(CustomJobRunnerConfig.INSTANCE)
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
    class MockPortsConfig {
        @Bean
        fun collectIdsPort(): CollectIdsPort = mockk(relaxed = true)

        @Bean
        fun collectPostsPort(): CollectPostsPort = mockk(relaxed = true)
    }

    @Configuration
    class CustomJobRunnerSupportConfig {
        companion object {
            val INSTANCE: JobRunnerSupport = mockk()
        }

        @Bean
        fun jobRunnerSupport(): JobRunnerSupport = INSTANCE
    }

    @Configuration
    class CustomJobRunnerConfig {
        companion object {
            val INSTANCE: JobRunner = mockk()
        }

        @Bean
        fun jobRunner(): JobRunner = INSTANCE
    }
}
