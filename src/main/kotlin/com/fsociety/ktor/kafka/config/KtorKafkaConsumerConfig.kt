package com.fsociety.ktor.kafka.config

import org.apache.kafka.common.serialization.Deserializer
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Configuration class for defining Kafka consumer settings in Ktor.
 *
 * This class extends `KtorKafkaConfig` and provides Kafka-specific consumer configurations.
 * It includes options for setting up deserializers for keys and values, assigning topics,
 * and adding custom properties necessary for consumer behavior.
 *
 * This configuration model is used to set up Kafka consumers dynamically within a Ktor application.
 *
 * @param K The key type for Kafka records.
 * @param V The value type for Kafka records.
 *
 * @property id A unique identifier for the consumer configuration instance.
 * @property keyDeserializer The deserializer class for Kafka record keys.
 * @property valueDeserializer The deserializer class for Kafka record values.
 * @property topics A list of topic names that the consumer should subscribe to.
 * @property extraProperties Additional Kafka consumer configuration properties, provided as key-value pairs.
 */
data class KtorKafkaConsumerConfig<K, V>(
    var id: String = UUID.randomUUID().toString(),
    override var bootstrapServers: String? = null,
    override var groupId: String? = null,
    var keyDeserializer: KClass<out Deserializer<K>>? = null,
    var valueDeserializer: KClass<out Deserializer<V>>? = null,
    var topics: List<String> = emptyList(),
    var extraProperties: Map<String, Any> = emptyMap(),
) : KtorKafkaConfig(
    bootstrapServers = bootstrapServers,
    groupId = groupId,
)
