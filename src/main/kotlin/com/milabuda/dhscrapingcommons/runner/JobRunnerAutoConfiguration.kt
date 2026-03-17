package com.milabuda.dhscrapingcommons.runner

import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
class JobRunnerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jobRunnerSupport(
        healthChecker: PortalHealthChecker,
        context: ApplicationContext,
        @Value("\${spring.application.name:scraper}") appName: String
    ): JobRunnerSupport = JobRunnerSupport(healthChecker, context, appName)

    @Bean
    @ConditionalOnBean(CollectIdsPort::class, CollectPostsPort::class)
    @ConditionalOnMissingBean
    fun jobRunner(
        runnerSupport: JobRunnerSupport,
        idCollector: CollectIdsPort,
        postCollector: CollectPostsPort,
    ): JobRunner = JobRunner(runnerSupport, idCollector, postCollector)
}
