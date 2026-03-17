package com.milabuda.dhscrapingcommons.imagearchive

interface ImageArchiver {

    fun saveAll(propertyId: String, imageUrls: List<PropertyImage>): List<ImageUploadResult>
}

