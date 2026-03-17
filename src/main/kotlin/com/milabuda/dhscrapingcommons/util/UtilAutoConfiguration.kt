package com.milabuda.dhscrapingcommons.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.ZoneOffset
import java.util.UUID

class IdProvider {
    fun uuid(): UUID = UUID.randomUUID()
}

@AutoConfiguration
class UtilAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun userAgentProvider(): UserAgentProvider = UserAgentProvider()

    @Bean
    @ConditionalOnMissingBean
    fun idProvider(): IdProvider = IdProvider()

    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock = Clock.system(ZoneOffset.UTC)

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()

    @Bean
    @ConditionalOnMissingBean
    fun observedAspect(observationRegistry: ObservationRegistry): ObservedAspect =
        ObservedAspect(observationRegistry)
}
