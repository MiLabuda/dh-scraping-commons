package com.milabuda.dhscrapingcommons.scraper

import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import io.github.resilience4j.retry.RetryRegistry
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class ScraperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun idsDocumentRetriever(
        userAgentProvider: UserAgentProvider,
        retryRegistry: RetryRegistry,
        observationRegistry: ObservationRegistry,
    ): IdsDocumentRetriever = IdsDocumentRetriever(userAgentProvider, retryRegistry, observationRegistry)

    @Bean
    @ConditionalOnMissingBean
    fun postDocumentRetriever(
        userAgentProvider: UserAgentProvider,
        retryRegistry: RetryRegistry,
        observationRegistry: ObservationRegistry,
    ): PostDocumentRetriever = PostDocumentRetriever(userAgentProvider, retryRegistry, observationRegistry)
}
