package com.milabuda.dhscrapingcommons.snapshot

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class PropertySnapshot(
    @get:DynamoDbPartitionKey
    var propertyId: String = "",
    var payload: String = "",
    var savedAt: String = ""
)
