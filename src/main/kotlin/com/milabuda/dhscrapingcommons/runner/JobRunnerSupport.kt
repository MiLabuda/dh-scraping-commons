package com.milabuda.dhscrapingcommons.runner

import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext

private val log = KotlinLogging.logger {}

class JobRunnerSupport(
    private val healthChecker: PortalHealthChecker,
    private val context: ApplicationContext,
    @Value("\${spring.application.name:scraper}") private val appName: String
) {

    /**
     * Checks portal health. Returns true if up, false if down.
     * Logs appropriately using the application name from spring.application.name.
     */
    fun checkHealthOrStop(): Boolean {
        return if (healthChecker.checkPortalStatus()) {
            log.info { "[$appName] Portal is up. Proceeding with job execution." }
            true
        } else {
            log.error { "[$appName] Portal is down. Aborting job execution." }
            stopRunner()
            false
        }
    }

    /**
     * Gracefully exits the Spring Boot application and halts the JVM.
     * Standard shutdown sequence for scraping jobs.
     */
    fun stopRunner(exitCode: Int = 0) {
        log.info { "[$appName] Finished work. Starting final shutdown sequence." }
        val code = SpringApplication.exit(context, { exitCode })
        log.info { "[$appName] Spring context closed. Termination in 2 seconds..." }

        try {
            Thread.sleep(2000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        Runtime.getRuntime().halt(code)
    }
}
