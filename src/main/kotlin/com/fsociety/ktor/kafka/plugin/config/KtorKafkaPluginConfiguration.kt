package com.fsociety.ktor.kafka.plugin.config

import com.fsociety.ktor.kafka.common.model.KafkaRegistration
import com.fsociety.ktor.kafka.common.model.config.KtorKafkaConsumerConfig
import com.fsociety.ktor.kafka.core.builder.KafkaKtorConsumerBuilder
import io.ktor.server.application.ApplicationEnvironment

class KtorKafkaPluginConfiguration private constructor(
    private val kafkaRegistrations: List<KafkaRegistration<*, *>>,
) {
    fun getKafkaRegistration() = kafkaRegistrations

    class Builder(environment: ApplicationEnvironment) {
        private val registrations = mutableListOf<KafkaRegistration<*, *>>()

        var bootstrapServers: String? = environment.config.propertyOrNull(PROPERTY_BOOSTRAP_SERVERS_PATH)?.getString()
        var groupId: String? = environment.config.propertyOrNull(PROPERTY_GROUP_ID_PATH)?.getString()

        fun <K : Any, V : Any> consumer(block: KafkaKtorConsumerBuilder<K, V>.() -> Unit) {
            val pluginConfig = KtorKafkaConsumerConfig<K, V>(
                bootstrapServers = bootstrapServers,
                groupId = groupId,
            )

            val builder = KafkaKtorConsumerBuilder(pluginConfig).apply(block)
            registrations.add(builder.build())
        }

        fun build(): KtorKafkaPluginConfiguration {
            return KtorKafkaPluginConfiguration(registrations.toList())
        }
    }

    companion object {
        private const val PROPERTY_BOOSTRAP_SERVERS_PATH = "ktor.kafka.bootstrap_servers"
        private const val PROPERTY_GROUP_ID_PATH = "ktor.kafka.groupId"
    }
}
