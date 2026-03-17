package com.milabuda.dhscrapingcommons.imagearchive

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.s3.bucket.images")
data class S3ImageBucketProperties(
    val name: String,
    val projectPrefix: String,
    val region: String,
)

