package com.milabuda.dhscrapingcommons.config

import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthProperties
import com.milabuda.dhscrapingcommons.runner.CollectIdsPort
import com.milabuda.dhscrapingcommons.runner.CollectPostsPort
import com.milabuda.dhscrapingcommons.runner.JobRunner
import com.milabuda.dhscrapingcommons.runner.JobRunnerSupport
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

class ScrapingCommonsAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ScrapingCommonsAutoConfiguration::class.java))
        .withUserConfiguration(WebClientBuilderConfig::class.java)
        .withPropertyValues("scraping.health.portal-url=http://localhost:8080")

    // -------------------------------------------------------------------------
    // Bean registration — happy path
    // -------------------------------------------------------------------------

    @Test
    fun `UserAgentProvider bean is registered by default`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(UserAgentProvider::class.java)
        }
    }

    @Test
    fun `PortalHealthChecker bean is registered by default`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(PortalHealthChecker::class.java)
        }
    }

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
    fun `user-provided UserAgentProvider bean is not replaced by auto-configuration`() {
        contextRunner
            .withUserConfiguration(CustomUserAgentProviderConfig::class.java)
            .run { ctx ->
                assertThat(ctx).hasSingleBean(UserAgentProvider::class.java)
                val provider = ctx.getBean(UserAgentProvider::class.java)
                assertThat(provider).isSameAs(CustomUserAgentProviderConfig.INSTANCE)
            }
    }

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
            .withConfiguration(AutoConfigurations.of(ScrapingCommonsAutoConfiguration::class.java))
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
    }

    @Configuration
    class MockPortsConfig {
        @Bean
        fun collectIdsPort(): CollectIdsPort = mockk(relaxed = true)

        @Bean
        fun collectPostsPort(): CollectPostsPort = mockk(relaxed = true)
    }

    @Configuration
    class CustomUserAgentProviderConfig {
        companion object {
            val INSTANCE: UserAgentProvider = mockk()
        }

        @Bean
        fun userAgentProvider(): UserAgentProvider = INSTANCE
    }

    @Configuration
    class CustomPortalHealthCheckerConfig {
        companion object {
            val INSTANCE: PortalHealthChecker = mockk()
        }

        @Bean
        fun portalHealthChecker(): PortalHealthChecker = INSTANCE
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
