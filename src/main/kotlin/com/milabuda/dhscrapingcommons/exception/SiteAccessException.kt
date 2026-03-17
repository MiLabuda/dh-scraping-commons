package com.milabuda.dhscrapingcommons.exception

class SiteAccessException(
    override val message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: "Problem accessing site: ", cause)