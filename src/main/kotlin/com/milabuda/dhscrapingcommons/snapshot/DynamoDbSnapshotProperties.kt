package com.milabuda.dhscrapingcommons.snapshot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.dynamodb")
data class DynamoDbSnapshotProperties(
    val snapshots: SnapshotsConfig = SnapshotsConfig()
) {
    data class SnapshotsConfig(
        val tableName: String = ""
    )
}
