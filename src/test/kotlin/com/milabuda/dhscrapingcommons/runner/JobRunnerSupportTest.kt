package com.milabuda.dhscrapingcommons.runner

import com.milabuda.dhscrapingcommons.healthcheck.PortalHealthChecker
import com.milabuda.dhscrapingcommons.util.TestConstants.DEFAULT_APP_NAME
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationContext

class JobRunnerSupportTest {

    private val healthChecker: PortalHealthChecker = mockk()
    private val context: ApplicationContext = mockk()

    private lateinit var jobRunnerSupport: JobRunnerSupport

    @BeforeEach
    fun setUp() {
        jobRunnerSupport = spyk(JobRunnerSupport(healthChecker, context, DEFAULT_APP_NAME))
    }

    @Test
    fun `checkHealthOrStop returns true when portal is up`() {
        every { healthChecker.checkPortalStatus() } returns true

        val result = jobRunnerSupport.checkHealthOrStop()

        assertThat(result).isTrue()
        verify(exactly = 1) { healthChecker.checkPortalStatus() }
    }

    @Test
    fun `checkHealthOrStop returns false and calls stopRunner when portal is down`() {
        every { healthChecker.checkPortalStatus() } returns false
        // Stub out stopRunner so it does NOT call Runtime.getRuntime().halt() in test JVM
        every { jobRunnerSupport.stopRunner(any()) } returns Unit

        val result = jobRunnerSupport.checkHealthOrStop()

        assertThat(result).isFalse()
        verify(exactly = 1) { healthChecker.checkPortalStatus() }
        verify(exactly = 1) { jobRunnerSupport.stopRunner(0) }
    }
}
