package com.milabuda.dhscrapingcommons.imagearchive

import java.io.InputStream

data class DownloadResult(
    val inputStream: InputStream,
    val contentLength: Long
)
