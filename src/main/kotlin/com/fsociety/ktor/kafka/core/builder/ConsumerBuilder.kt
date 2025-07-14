package com.fsociety.ktor.kafka.core.builder

import com.fsociety.ktor.kafka.common.model.KtorKafkaConsumerRegistration
import com.fsociety.ktor.kafka.common.utils.createConsumer
import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer
import io.ktor.server.application.Application
import org.apache.kafka.common.serialization.Deserializer
import java.util.UUID

class ConsumerBuilder<K, V>(
    // TODO: Use the env for other get variables
    private val application: Application,
) {
    var id: String = UUID.randomUUID().toString()
    var bootstrapServers: String? = null
    var groupId: String? = null
    var topics: List<String> = emptyList()

    var consumer: KtorKafkaConsumer<K, V>? = null
    var valueDeserializer: Deserializer<V>? = null
    var keyDeserializer: Deserializer<K>? = null

    private var listener: ((K, V) -> Unit)? = null
    fun listener(block: (K, V) -> Unit) {
        listener = block
    }

    internal fun build(): KtorKafkaConsumerRegistration<K, V> {
        val validListener = requireNotNull(listener) {
            "No listener provided. Please define a listener using the listener { } function"
        }

        consumer?.let { consumer ->
            return KtorKafkaConsumerRegistration(id, consumer, validListener)
        }

        val validBoostrapServers = requireNotNull(bootstrapServers) {
            "Bootstrap servers configuration is missing. Please provide the Kafka broker addresses"
        }
        val validGroupId = requireNotNull(groupId) {
            "Consumer group ID is missing. Please specify a group ID for this consumer"
        }

        require(topics.isNotEmpty()) {
            "No topics specified. Please provide at least one topic to subscribe to"
        }

        val validKeyDeserializer = requireNotNull(keyDeserializer) {
            "Key deserializer is missing. Please provide a deserializer for the message keys"
        }
        val validValueDeserializer = requireNotNull(valueDeserializer) {
            "Value deserializer is missing. Please provide a deserializer for the message values"
        }
        return KtorKafkaConsumerRegistration(
            id = id,
            listener = validListener,
            ktorKafkaConsumer = KtorKafkaConsumer(
                kafkaConsumer = createConsumer(
                    bootstrapServers = validBoostrapServers,
                    valueDeserializer = validValueDeserializer,
                    keyDeserializer = validKeyDeserializer,
                    groupId = validGroupId,
                ),
                topics = topics,
            ),
        )
    }
}
