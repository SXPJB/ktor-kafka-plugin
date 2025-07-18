package com.fsociety.ktor.kafka.core.registration

import com.fsociety.ktor.kafka.common.model.KafkaRegistration
import com.fsociety.ktor.kafka.common.model.KtorKafkaConsumerRegistration
import com.fsociety.ktor.kafka.common.model.KtorKafkaProducerRegistration
import com.fsociety.ktor.kafka.plugin.KtorKafkaPlugin

class KafkaRegistrationHandler(
    private val plugin: KtorKafkaPlugin,
) {

    fun <K, V> handle(registration: KafkaRegistration<K, V>) {
        when (registration) {
            is KtorKafkaConsumerRegistration -> add(registration)
            is KtorKafkaProducerRegistration -> add(registration)
        }
    }

    private fun <K, V> add(
        ktorKafkaConsumer: KtorKafkaConsumerRegistration<K, V>,
    ) {
        plugin.addConsumer(
            id = ktorKafkaConsumer.id,
            consumer = ktorKafkaConsumer.ktorKafkaConsumer,
            listener = ktorKafkaConsumer.listener,
        )
    }

    private fun <K, V> add(
        ktorKafkaProducer: KtorKafkaProducerRegistration<K, V>,
    ) {
        plugin.addProducer(
            id = ktorKafkaProducer.id,
            producer = ktorKafkaProducer.ktorKafkaProducer,
        )
    }
}
