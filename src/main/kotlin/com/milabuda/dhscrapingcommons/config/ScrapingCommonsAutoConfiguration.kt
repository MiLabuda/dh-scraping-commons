package com.milabuda.dhscrapingcommons.config

import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthProperties
import com.milabuda.dhscrapingcommons.runner.CollectIdsPort
import com.milabuda.dhscrapingcommons.runner.CollectPostsPort
import com.milabuda.dhscrapingcommons.runner.JobRunner
import com.milabuda.dhscrapingcommons.runner.JobRunnerSupport
import com.milabuda.dhscrapingcommons.scraper.IdsDocumentRetriever
import com.milabuda.dhscrapingcommons.scraper.PostDocumentRetriever
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.github.resilience4j.retry.RetryRegistry
import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(PortalHealthProperties::class)
class ScrapingCommonsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun userAgentProvider(): UserAgentProvider = UserAgentProvider()

    @Bean
    @ConditionalOnMissingBean
    fun portalHealthChecker(
        webClientBuilder: WebClient.Builder,
        userAgentProvider: UserAgentProvider,
        properties: PortalHealthProperties
    ): PortalHealthChecker = PortalHealthChecker(webClientBuilder, userAgentProvider, properties)

    @Bean
    @ConditionalOnMissingBean
    fun jobRunnerSupport(
        healthChecker: PortalHealthChecker,
        context: ApplicationContext,
        @Value("\${spring.application.name:scraper}") appName: String
    ): JobRunnerSupport = JobRunnerSupport(healthChecker, context, appName)

    @Bean
    @ConditionalOnBean(ObservationRegistry::class, RetryRegistry::class)
    @ConditionalOnMissingBean
    fun idsDocumentRetriever(
        userAgentProvider: UserAgentProvider,
        retryRegistry: RetryRegistry,
        observationRegistry: ObservationRegistry,
    ): IdsDocumentRetriever = IdsDocumentRetriever(userAgentProvider, retryRegistry, observationRegistry)

    @Bean
    @ConditionalOnBean(ObservationRegistry::class, RetryRegistry::class)
    @ConditionalOnMissingBean
    fun postDocumentRetriever(
        userAgentProvider: UserAgentProvider,
        retryRegistry: RetryRegistry,
        observationRegistry: ObservationRegistry,
    ): PostDocumentRetriever = PostDocumentRetriever(userAgentProvider, retryRegistry, observationRegistry)

    @Bean
    @ConditionalOnBean(CollectIdsPort::class, CollectPostsPort::class)
    @ConditionalOnMissingBean
    fun jobRunner(
        runnerSupport: JobRunnerSupport,
        idCollector: CollectIdsPort,
        postCollector: CollectPostsPort,
    ): JobRunner = JobRunner(runnerSupport, idCollector, postCollector)
}
