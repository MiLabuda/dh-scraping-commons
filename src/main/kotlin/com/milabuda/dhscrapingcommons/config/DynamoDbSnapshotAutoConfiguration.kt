package com.milabuda.dhscrapingcommons.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.milabuda.dhscrapingcommons.snapshot.DynamoDbSnapshotProperties
import com.milabuda.dhscrapingcommons.snapshot.DynamoDbSnapshotRepository
import com.milabuda.dhscrapingcommons.snapshot.PropertySnapshot
import com.milabuda.dhscrapingcommons.snapshot.SnapshotPort
import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver
import io.awspring.cloud.dynamodb.DynamoDbTemplate
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(DynamoDbTemplate::class)
@EnableConfigurationProperties(DynamoDbSnapshotProperties::class)
class DynamoDbSnapshotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun dynamoDbTableNameResolver(props: DynamoDbSnapshotProperties): DynamoDbTableNameResolver =
        object : DynamoDbTableNameResolver {
            override fun <T : Any> resolve(clazz: Class<T>): String = when (clazz) {
                PropertySnapshot::class.java -> props.snapshots.tableName
                else -> clazz.simpleName
            }
        }

    @Bean
    @ConditionalOnMissingBean
    fun snapshotPort(
        dynamoDbTemplate: DynamoDbTemplate,
        objectMapper: ObjectMapper
    ): SnapshotPort = DynamoDbSnapshotRepository(dynamoDbTemplate, objectMapper)
}
