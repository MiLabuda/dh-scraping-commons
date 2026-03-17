package com.milabuda.dhscrapingcommons.util

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class UtilAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun userAgentProvider(): UserAgentProvider = UserAgentProvider()
}
