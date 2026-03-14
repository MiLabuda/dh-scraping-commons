package com.milabuda.dhscrapingcommons.snapshot

interface SnapshotPort {
    fun save(propertyId: String, scraped: ScrappedBaseDto)
}
