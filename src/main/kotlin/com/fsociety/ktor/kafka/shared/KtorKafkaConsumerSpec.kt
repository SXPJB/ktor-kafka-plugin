package com.fsociety.ktor.kafka.shared

import com.fsociety.ktor.kafka.consumer.KtorKafkaConsumer

/**
 * Represents the specification for a consumer in the Ktor Kafka integration framework.
 *
 * This data class provides the necessary configuration and callback to enable
 * Kafka message consumption using a `KtorKafkaConsumer`. It is part of the
 * `KtorKafkaSpec` contract, allowing it to be managed and registered within
 * the framework's consumer management workflow.
 *
 * @param K The type of keys in Kafka messages.
 * @param V The type of values in Kafka messages.
 * @property id A unique identifier for the Kafka consumer specification instance.
 * @property ktorKafkaConsumer The `KtorKafkaConsumer` instance that provides the logic needed
 * for consuming messages from Kafka topics.
 * @property listener A callback function that processes each consumed Kafka message.
 * The function receives the key-value pair of each record consumed.
 */
data class KtorKafkaConsumerSpec<K, V>(
    override val id: String,
    val ktorKafkaConsumer: KtorKafkaConsumer<K, V>,
    val listener: (K, V) -> Unit,
) : KtorKafkaSpec
