package com.fsociety.ktor.kafka.common.model

import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer

data class KafkaConsumerWrapper<K, V>(
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
