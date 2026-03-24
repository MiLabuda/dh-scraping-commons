package com.milabuda.dhscrapingcommons.imagearchive

import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.InputStream

interface S3MediatorPort {
    suspend fun delegateUploadToS3(
        putObjectRequest: PutObjectRequest,
        inputStream: InputStream,
        contentSize: Long,
    ): PutObjectResponse
}
