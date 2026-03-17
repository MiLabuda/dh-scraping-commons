package com.milabuda.dhscrapingcommons.runner

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

private val log = KotlinLogging.logger {}

class JobRunner(
    private val runnerSupport: JobRunnerSupport,
    private val idCollector: CollectIdsPort,
    private val postCollector: CollectPostsPort,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        runnerSupport.checkHealthOrStop()
        log.info { "Starting daily job..." }
        idCollector.collectIds()

        log.info { "Running Post collection..." }
        postCollector.collectPosts()

        log.info { "Successfully finished job." }
        runnerSupport.stopRunner()
    }
}
