package com.fsociety.ktor.kafka.shared

import com.fsociety.ktor.kafka.producer.KtorKafkaProducer

data class KtorKafkaProducerSpec<K, V>(
    override val id: String,
    val ktorKafkaProducer: KtorKafkaProducer<K, V>
) : KtorKafkaSpec
