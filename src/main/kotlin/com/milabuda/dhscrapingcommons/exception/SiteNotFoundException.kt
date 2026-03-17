package com.milabuda.dhscrapingcommons.exception

class SiteNotFoundException(
    override val message: String? = null,
    cause: Throwable? = null
    ) : RuntimeException(message ?: "Site not found: ", cause)
