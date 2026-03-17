package com.milabuda.dhscrapingcommons.healthcheck

import com.milabuda.dhscrapingcommons.util.UserAgentProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(PortalHealthProperties::class)
class HealthCheckAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun portalHealthChecker(
        webClientBuilder: WebClient.Builder,
        userAgentProvider: UserAgentProvider,
        properties: PortalHealthProperties
    ): PortalHealthChecker = PortalHealthChecker(webClientBuilder, userAgentProvider, properties)
}
