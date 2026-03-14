package com.milabuda.dhscrapingcommons.snapshot

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.dynamodb.DynamoDbTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant

private val log = KotlinLogging.logger {}

class DynamoDbSnapshotRepository(
    private val dynamoDbTemplate: DynamoDbTemplate,
    private val objectMapper: ObjectMapper
) : SnapshotPort {

    override fun save(propertyId: String, scraped: ScrappedBaseDto) {
        runCatching {
            val snapshot = PropertySnapshot(
                propertyId = propertyId,
                payload = objectMapper.writeValueAsString(scraped),
                savedAt = Instant.now().toString()
            )
            dynamoDbTemplate.save(snapshot)
        }
            .onSuccess { log.debug { "Snapshot saved to DynamoDB for propertyId: [$propertyId]" } }
            .onFailure { e -> log.warn(e) { "Failed to save snapshot to DynamoDB for propertyId: [$propertyId]" } }
    }
}
