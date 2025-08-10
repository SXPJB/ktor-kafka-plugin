package com.fsociety.ktor.kafka.config

/**
 * Base configuration class for Kafka integration in Ktor.
 *
 * This sealed class serves as the foundation for defining configurations
 * for Kafka producers and consumers within a Ktor application. It provides
 * common properties such as `bootstrapServers` and `groupId` required
 * for establishing Kafka connections.
 *
 * @property bootstrapServers The Kafka bootstrap servers used to initialize the connection.
 * Can be null if not provided.
 * @property groupId The consumer group ID used for Kafka consumer configurations.
 * Can be null if not provided.
 */
sealed class KtorKafkaConfig(
    open var bootstrapServers: String?,
    open var groupId: String?,
)
