package com.fsociety.ktor.kafka.core.consumer.manager

import com.fsociety.ktor.kafka.common.model.KafkaConsumerWrapper
import com.fsociety.ktor.kafka.common.utils.logger
import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class KtorKafkaConsumerManager {
    private val logger = logger()
    private val consumers = mutableMapOf<String, KafkaConsumerWrapper<*, *>>()

    fun <K, V> create(
        id: String,
        consumer: KtorKafkaConsumer<K, V>,
        listener: (K, V) -> Unit,
    ) {
        if (consumers.containsKey(id)) {
            logger.warn("Consumer $id is already added. Skipping...")
            return
        }
        consumers[id] = KafkaConsumerWrapper(consumer, listener)
        logger.info("Consumer $id is added")
    }

    fun startAll(scope: CoroutineScope) {
        logger.info("Starting kafka consumers")
        consumers.forEach { (id, consumer) ->
            scope.launch {
                logger.info("Starting kafka consumer with id $id")
                consumer.startListening()
            }
        }
    }

    fun stopAll() {
        logger.info("Stopping kafka consumers")
        consumers.forEach { (id, consumer) ->
            logger.info("Stopping kafka consumer with id $id")
            consumer.stopListening()
        }
    }
}
