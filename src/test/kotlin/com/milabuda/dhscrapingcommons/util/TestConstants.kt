package com.milabuda.dhscrapingcommons.util

object TestConstants {

    const val DEFAULT_APP_NAME = "test-scraper"
    const val CACHE_KEY_STATUS = "status"
    const val WIREMOCK_BASE_URL_PROPERTY = "scraping.health.portal-url"

    // Browser header names
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
    const val HEADER_UPGRADE_INSECURE_REQUESTS = "Upgrade-Insecure-Requests"
    const val HEADER_SEC_FETCH_DEST = "Sec-Fetch-Dest"
    const val HEADER_SEC_FETCH_MODE = "Sec-Fetch-Mode"
    const val HEADER_SEC_FETCH_SITE = "Sec-Fetch-Site"
    const val HEADER_SEC_FETCH_USER = "Sec-Fetch-User"
    const val HEADER_CACHE_CONTROL = "Cache-Control"

    // Expected browser header values
    const val ACCEPT_VALUE = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"
    const val ACCEPT_LANGUAGE_VALUE = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7"
    const val UPGRADE_INSECURE_REQUESTS_VALUE = "1"
    const val SEC_FETCH_DEST_VALUE = "document"
    const val SEC_FETCH_MODE_VALUE = "navigate"
    const val SEC_FETCH_SITE_VALUE = "none"
    const val SEC_FETCH_USER_VALUE = "?1"
    const val CACHE_CONTROL_VALUE = "max-age=0"

    const val BROWSER_HEADERS_COUNT = 8

    // Health check probe path
    const val HEALTH_CHECK_PATH = "/health"
}
