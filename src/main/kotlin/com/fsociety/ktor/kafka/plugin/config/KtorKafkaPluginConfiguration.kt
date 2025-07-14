package com.fsociety.ktor.kafka.plugin.config

import com.fsociety.ktor.kafka.common.model.KafkaRegistration
import com.fsociety.ktor.kafka.core.builder.ConsumerBuilder
import io.ktor.server.application.Application

class KtorKafkaPluginConfiguration private constructor(
    private val kafkaRegistrations: List<KafkaRegistration<*, *>>,
) {
    fun getKafkaRegistration() = kafkaRegistrations

    class Builder(private val application: Application) {
        private val registrations = mutableListOf<KafkaRegistration<*, *>>()
        var bootstrapServers: String? = null
        var groupId: String? = null

        fun <K : Any, V : Any> consumer(block: ConsumerBuilder<K, V>.() -> Unit) {
            val builder = ConsumerBuilder<K, V>(application).apply {
                this.bootstrapServers = this@Builder.bootstrapServers
                this.groupId = this@Builder.groupId
                block()
            }
            registrations.add(builder.build())
        }

        fun build(): KtorKafkaPluginConfiguration {
            return KtorKafkaPluginConfiguration(registrations.toList())
        }
    }
}
