package com.milabuda.dhscrapingcommons.imagearchive

interface ImageDownloaderPort {
    fun downloadImage(photoUrl: String): Result<DownloadResult>
}
