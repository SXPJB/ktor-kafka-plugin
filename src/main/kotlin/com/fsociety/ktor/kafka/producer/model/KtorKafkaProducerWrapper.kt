package com.fsociety.ktor.kafka.producer.model

import com.fsociety.ktor.kafka.producer.KtorKafkaProducer

data class KtorKafkaProducerWrapper<K, V>(
    val producer: KtorKafkaProducer<K, V>
)
