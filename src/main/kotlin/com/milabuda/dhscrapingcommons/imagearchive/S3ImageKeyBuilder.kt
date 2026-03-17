package com.milabuda.dhscrapingcommons.imagearchive

import java.time.LocalDate

private val DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")

class S3ImageKeyBuilder(
    private val s3props: S3ImageBucketProperties,
) {

    fun buildKey(propertyId: String, photoUrl: String?, sequenceNumber: Int): String {
        val datePath = LocalDate.now().format(DATE_FORMATTER)
        val fileNamePrefix = formatWithLeadingZeros(sequenceNumber)
        val fileName = extractFileName(photoUrl)

        return "${s3props.projectPrefix}/" +
                "${s3props.region}/" +
                "$datePath/" +
                "property_$propertyId/" +
                "${fileNamePrefix}_$fileName"
    }

    private fun formatWithLeadingZeros(sequenceNumber: Int) =
        sequenceNumber.toString().padStart(3, '0')

    private fun extractFileName(photoUrl: String?): String {
        if (photoUrl.isNullOrEmpty()) return "unknown_file.jpg"

        val urlWithoutParams = photoUrl
            .substringBefore('?')
            .substringBefore(';')

        val fileName = urlWithoutParams.substringAfterLast('/')

        return when {
            fileName.isEmpty() -> "unknown_file.jpg"
            !fileName.contains('.') -> "$fileName.jpg"
            else -> fileName
        }
    }
}
