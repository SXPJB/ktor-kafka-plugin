package com.fsociety.ktor.kafka.common.model

import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer
import com.fsociety.ktor.kafka.core.producer.KtorKafkaProducer

sealed interface KafkaRegistration<K, V> {
    val id: String
}

data class KtorKafkaConsumerRegistration<K, V>(
    override val id: String,
    val ktorKafkaConsumer: KtorKafkaConsumer<K, V>,
    val listener: (K, V) -> Unit,
) : KafkaRegistration<K, V>

data class KtorKafkaProducerRegistration<K, V>(
    override val id: String,
    val ktorKafkaProducer: KtorKafkaProducer<K, V>,
) : KafkaRegistration<K, V>
