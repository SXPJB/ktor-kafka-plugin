package com.fsociety.ktor.kafka.core.registration

import com.fsociety.ktor.kafka.common.model.KafkaRegistration
import com.fsociety.ktor.kafka.common.model.KtorKafkaConsumerRegistration
import com.fsociety.ktor.kafka.plugin.KtorKafkaPlugin

class KafkaRegistrationHandler(
    private val plugin: KtorKafkaPlugin,
) {

    fun <K, V> handle(registration: KafkaRegistration<K, V>) {
        when (registration) {
            is KtorKafkaConsumerRegistration -> registerConsumer(registration)
        }
    }

    private fun <K, V> registerConsumer(
        registration: KtorKafkaConsumerRegistration<K, V>,
    ) {
        plugin.addConsumer(
            id = registration.id,
            consumer = registration.ktorKafkaConsumer,
            listener = registration.listener,
        )
    }
}
