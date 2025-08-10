package com.fsociety.ktor.kafka.shared

/**
 * Represents a specification for Ktor Kafka components.
 *
 * This sealed interface acts as a common contract for various Kafka-related specifications
 * in the Ktor Kafka integration framework. Implementers of this interface provide
 * specific configurations or setups necessary for the Kafka integration.
 *
 * @property id A unique identifier representing the specific Kafka specification instance.
 */
sealed interface KtorKafkaSpec {
    val id: String
}
