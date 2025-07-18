package com.fsociety.ktor.kafka.core.builder

import com.fsociety.ktor.kafka.common.model.KtorKafkaProducerRegistration
import com.fsociety.ktor.kafka.common.utils.createProducer
import com.fsociety.ktor.kafka.core.producer.KtorKafkaProducer
import io.ktor.server.application.Application
import org.apache.kafka.common.serialization.Serializer
import java.util.UUID

class ProducerBuilder<K, V>(
    private val application: Application,
) {
    var id: String = UUID.randomUUID().toString()
    var ktorKafkaProducer: KtorKafkaProducer<K, V>? = null

    var bootstrapServers: String? = null
    var topic: String? = null

    var keySerializer: Serializer<K>? = null
    var valueSerializer: Serializer<V>? = null

    internal fun build(): KtorKafkaProducerRegistration<K, V> {
        ktorKafkaProducer?.let { validProducer ->
            return KtorKafkaProducerRegistration(id, validProducer)
        }

        val validValueSerializer = requireNotNull(valueSerializer) {
            "Either producer or valueSerializer must be provided"
        }

        val validKeySerializer = requireNotNull(keySerializer) {
            "Either producer or keySerializer must be provided"
        }

        val validBootstrapServers = requireNotNull(bootstrapServers) {
            "Either producer, bootstrapServers, or kafka.bootstrapServers in environment must be provided"
        }

        val validTopic = requireNotNull(topic) {
            "Either producer or topic must be provided"
        }

        return KtorKafkaProducerRegistration(
            id = id,
            ktorKafkaProducer = KtorKafkaProducer(
                kafkaProducer = createProducer(
                    bootstrapServers = validBootstrapServers,
                    keySerializer = validKeySerializer,
                    valueSerializer = validValueSerializer,
                ),
                topic = validTopic,
            ),
        )
    }
}
