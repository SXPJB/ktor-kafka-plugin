package com.fsociety.ktor.kafka.config

import com.fsociety.ktor.kafka.consumer.builder.KafkaKtorConsumerBuilder
import com.fsociety.ktor.kafka.shared.KtorKafkaSpec
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString

/**
 * Configuration class for integrating Kafka with a Ktor application using a plugin-based approach.
 *
 * This class extends `KtorKafkaConfig` and provides methods to configure Kafka consumers. It manages
 * a list of Kafka consumer registrations and allows for the dynamic construction of Kafka consumers
 * at runtime.
 *
 * @constructor Initializes the configuration using an instance of `ApplicationConfig`.
 * The constructor attempts to retrieve required properties like `bootstrap.servers` and `group.id`
 * from the provided configuration.
 *
 * @param config The `ApplicationConfig` instance containing configuration properties for the Kafka consumer.
 */
data class KtorKafkaPluginConfiguration(
    val config: ApplicationConfig,
) : KtorKafkaConfig(
    bootstrapServers = config.tryGetString(PROPERTY_BOOTSTRAP_SERVERS_PATH),
    groupId = config.tryGetString(PROPERTY_GROUP_ID_PATH),
) {
    private val registrationsMutable = mutableListOf<KtorKafkaSpec>()
    internal val registrations
        get() = registrationsMutable.toList()

    fun <K : Any, V : Any> consumer(block: KafkaKtorConsumerBuilder<K, V>.() -> Unit) {
        val pluginDefaults = KtorKafkaConsumerConfig<K, V>(
            bootstrapServers = bootstrapServers,
            groupId = groupId,
        )
        val builder = KafkaKtorConsumerBuilder<K, V>(pluginDefaults).apply(block)
        registrationsMutable += builder.build()
    }

    companion object {
        private const val PROPERTY_BOOTSTRAP_SERVERS_PATH = "bootstrap_servers"
        private const val PROPERTY_GROUP_ID_PATH = "groupId"
    }
}
