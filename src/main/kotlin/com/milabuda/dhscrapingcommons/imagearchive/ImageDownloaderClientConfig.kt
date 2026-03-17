package com.milabuda.dhscrapingcommons.imagearchive

import java.net.http.HttpClient
import java.time.Duration

internal fun defaultImageDownloaderHttpClient(): HttpClient =
    HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
