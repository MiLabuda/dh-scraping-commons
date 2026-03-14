package com.milabuda.dhscrapingcommons.snapshot

/**
 * Marker interface for scraped page data DTOs.
 * Each scraper application implements this interface with its own set of fields.
 * The library handles serialization to DynamoDB automatically via Jackson.
 */
interface ScrappedBaseDto
