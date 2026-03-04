package com.milabuda.dhscrapingcommons.util

val BROWSER_HEADERS: Map<String, String> = mapOf(
    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Accept-Language" to "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7",
    "Upgrade-Insecure-Requests" to "1",
    "Sec-Fetch-Dest" to "document",
    "Sec-Fetch-Mode" to "navigate",
    "Sec-Fetch-Site" to "none",
    "Sec-Fetch-User" to "?1",
    "Cache-Control" to "max-age=0"
)
