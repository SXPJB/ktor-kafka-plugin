package com.fsociety.ktor.kafka.core.registration.manager

import com.fsociety.ktor.kafka.common.utils.logger
import com.fsociety.ktor.kafka.core.producer.KtorKafkaProducer

class KtorKafkaProducerManager {
    private val logger = logger()
    private val producers = mutableMapOf<String, KtorKafkaProducer<*, *>>()

    fun <K, V> create(
        id: String,
        producer: KtorKafkaProducer<K, V>,
    ) {
        if (producers.containsKey(id)) {
            logger.warn("Kafka producer $id is already registered")
            return
        }
        producers[id] = producer
        logger.info("Producer $id is registered")
    }

    fun closeAll() {
        producers.forEach { (id, producer) ->
            logger.info("Closing kafka producer with id $id")
            producer.close()
        }
    }
}
