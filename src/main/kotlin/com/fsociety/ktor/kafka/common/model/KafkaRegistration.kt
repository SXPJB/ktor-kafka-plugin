package com.fsociety.ktor.kafka.common.model

import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer

sealed interface KafkaRegistration<K, V> {
    val id: String
}

data class KtorKafkaConsumerRegistration<K, V>(
    override val id: String,
    val ktorKafkaConsumer: KtorKafkaConsumer<K, V>,
    val listener: (K, V) -> Unit,
) : KafkaRegistration<K, V>
