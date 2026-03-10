package com.milabuda.dhscrapingcommons.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserAgentProviderTest {

    private val provider = UserAgentProvider()

    @Test
    fun `provide returns non-blank string`() {
        val result = provider.provide()

        assertThat(result).isNotBlank()
    }

    @Test
    fun `provide returns string that looks like a valid User-Agent`() {
        val result = provider.provide()

        assertThat(result).startsWith("Mozilla/5.0")
    }

    @Test
    fun `provide shows randomness across multiple calls`() {
        val results = (1..100).map { provider.provide() }.toSet()

        assertThat(results.size).isGreaterThan(1)
    }

    @Test
    fun `provide never returns the same string exclusively across many calls`() {
        val first = provider.provide()
        val allSame = (1..50).all { provider.provide() == first }

        assertThat(allSame).isFalse()
    }
}
