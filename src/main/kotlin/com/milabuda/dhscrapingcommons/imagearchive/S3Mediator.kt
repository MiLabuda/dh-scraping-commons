package com.milabuda.dhscrapingcommons.imagearchive

import io.micrometer.observation.annotation.Observed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.InputStream

open class S3Mediator(
    private val s3Client: S3Client,
) : S3MediatorPort {

    @Observed(
        name = "property.post.collection.image.upload",
        contextualName = "Uploading property post images to S3",
        lowCardinalityKeyValues = ["operation", "uploadPosts"],
    )
    override suspend fun delegateUploadToS3(
        putObjectRequest: PutObjectRequest,
        inputStream: InputStream,
        contentSize: Long,
    ): PutObjectResponse = withContext(Dispatchers.IO) {
        inputStream.use { s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(it, contentSize)) }
    }
}
