package com.fsociety.ktor.kafka.consumer.model

import com.fsociety.ktor.kafka.consumer.KtorKafkaConsumer

/**
 * Wrapper class that combines a `KtorKafkaConsumer` and a message listener function.
 *
 * This class simplifies the process of associating a specific `KtorKafkaConsumer`
 * with a callback function to handle Kafka messages. It provides methods to
 * start and stop listening for messages.
 *
 * @param K The type of keys in Kafka messages.
 * @param V The type of values in Kafka messages.
 * @property consumer The `KtorKafkaConsumer` instance responsible for consuming Kafka messages.
 * @property listener A callback function that processes each consumed Kafka message.
 * The function receives the key-value pair of each record consumed.
 */
data class KtorKafkaConsumerWrapper<K, V>(
    val consumer: KtorKafkaConsumer<K, V>,
    val listener: (K, V) -> Unit,
) {

    fun startListening() {
        consumer.startListening(listener)
    }

    fun stopListening() {
        consumer.stopListening()
    }
}
