package com.milabuda.dhscrapingcommons.util

import com.milabuda.dhscrapingcommons.util.TestConstants.ACCEPT_LANGUAGE_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.ACCEPT_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.BROWSER_HEADERS_COUNT
import com.milabuda.dhscrapingcommons.util.TestConstants.CACHE_CONTROL_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_ACCEPT
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_ACCEPT_LANGUAGE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_CACHE_CONTROL
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_DEST
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_MODE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_SITE
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_SEC_FETCH_USER
import com.milabuda.dhscrapingcommons.util.TestConstants.HEADER_UPGRADE_INSECURE_REQUESTS
import com.milabuda.dhscrapingcommons.util.TestConstants.SEC_FETCH_DEST_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.SEC_FETCH_MODE_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.SEC_FETCH_SITE_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.SEC_FETCH_USER_VALUE
import com.milabuda.dhscrapingcommons.util.TestConstants.UPGRADE_INSECURE_REQUESTS_VALUE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BrowserHeadersTest {

    @Test
    fun `BROWSER_HEADERS contains all required header keys`() {
        assertThat(BROWSER_HEADERS.keys).containsExactlyInAnyOrder(
            HEADER_ACCEPT,
            HEADER_ACCEPT_LANGUAGE,
            HEADER_UPGRADE_INSECURE_REQUESTS,
            HEADER_SEC_FETCH_DEST,
            HEADER_SEC_FETCH_MODE,
            HEADER_SEC_FETCH_SITE,
            HEADER_SEC_FETCH_USER,
            HEADER_CACHE_CONTROL,
        )
    }

    @Test
    fun `BROWSER_HEADERS has exactly the expected number of entries`() {
        assertThat(BROWSER_HEADERS).hasSize(BROWSER_HEADERS_COUNT)
    }

    @Test
    fun `BROWSER_HEADERS has correct Accept value`() {
        assertThat(BROWSER_HEADERS[HEADER_ACCEPT]).isEqualTo(ACCEPT_VALUE)
    }

    @Test
    fun `BROWSER_HEADERS has correct Accept-Language value`() {
        assertThat(BROWSER_HEADERS[HEADER_ACCEPT_LANGUAGE]).isEqualTo(ACCEPT_LANGUAGE_VALUE)
    }

    @Test
    fun `BROWSER_HEADERS has correct Sec-Fetch header values`() {
        assertThat(BROWSER_HEADERS[HEADER_SEC_FETCH_DEST]).isEqualTo(SEC_FETCH_DEST_VALUE)
        assertThat(BROWSER_HEADERS[HEADER_SEC_FETCH_MODE]).isEqualTo(SEC_FETCH_MODE_VALUE)
        assertThat(BROWSER_HEADERS[HEADER_SEC_FETCH_SITE]).isEqualTo(SEC_FETCH_SITE_VALUE)
        assertThat(BROWSER_HEADERS[HEADER_SEC_FETCH_USER]).isEqualTo(SEC_FETCH_USER_VALUE)
    }

    @Test
    fun `BROWSER_HEADERS has correct Upgrade-Insecure-Requests value`() {
        assertThat(BROWSER_HEADERS[HEADER_UPGRADE_INSECURE_REQUESTS]).isEqualTo(UPGRADE_INSECURE_REQUESTS_VALUE)
    }

    @Test
    fun `BROWSER_HEADERS has correct Cache-Control value`() {
        assertThat(BROWSER_HEADERS[HEADER_CACHE_CONTROL]).isEqualTo(CACHE_CONTROL_VALUE)
    }
}
