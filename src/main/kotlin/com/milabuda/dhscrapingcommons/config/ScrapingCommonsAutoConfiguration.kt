package com.milabuda.dhscrapingcommons.config

import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class ScrapingCommonsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun userAgentProvider(): UserAgentProvider = UserAgentProvider()

    @Bean
    @ConditionalOnMissingBean
    fun portalHealthChecker(
        webClientBuilder: org.springframework.web.reactive.function.client.WebClient.Builder,
        userAgentProvider: UserAgentProvider,
        @org.springframework.beans.factory.annotation.Value("\${scraping.health.portal-url}") portalUrl: String
    ): PortalHealthChecker = PortalHealthChecker(webClientBuilder, userAgentProvider, portalUrl)
}
