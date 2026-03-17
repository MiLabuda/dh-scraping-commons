package com.milabuda.dhscrapingcommons.imagearchive

sealed class ImageDownloadException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {

    class NotFound(url: String) :
        ImageDownloadException("Image not found (404): $url, probably post deleted")

    class ClientError(url: String, val statusCode: Int) :
        ImageDownloadException("Client error ($statusCode) for: $url")

    class ServerError(url: String, val statusCode: Int) :
        ImageDownloadException("Server error ($statusCode) for: $url")

    class UnexpectedStatus(url: String, val statusCode: Int) :
        ImageDownloadException("Unexpected status ($statusCode) for: $url")

    class MissingContentLength(url: String) :
        ImageDownloadException("Missing or invalid Content-Length for: $url")
}