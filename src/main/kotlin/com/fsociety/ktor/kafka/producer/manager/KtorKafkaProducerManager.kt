package com.fsociety.ktor.kafka.producer.manager

import com.fsociety.ktor.kafka.producer.model.KtorKafkaProducerWrapper
import com.fsociety.ktor.kafka.shared.KtorKafkaProducerSpec
import com.fsociety.ktor.kafka.utils.logger
import java.util.concurrent.ConcurrentHashMap

class KtorKafkaProducerManager {
    private val log = logger()
    private val producers = ConcurrentHashMap<String, KtorKafkaProducerWrapper<*, *>>()

    fun <K, V> register(entry: KtorKafkaProducerSpec<K, V>) {
        val (id) = entry
        if (producers.putIfAbsent(id, entry.toWrapper()) == null) {
            log.info(PRODUCER_REGISTERED_EVENT, id)
        } else {
            log.warn(PRODUCER_ALREADY_REGISTERED, id)
        }
    }

    fun getProducer(id: String): KtorKafkaProducerWrapper<*, *>? {
        return producers[id]
    }

    fun closeAll() {
        log.info(CLOSE_PRODUCER_EVENT)
        producers.forEach { (id, wrapper) ->
            wrapper.producer.close()
        }
    }

    private fun <K, V> KtorKafkaProducerSpec<K, V>.toWrapper(): KtorKafkaProducerWrapper<K, V> {
        return KtorKafkaProducerWrapper(
            ktorKafkaProducer
        )
    }

    private companion object {
        const val PRODUCER_REGISTERED_EVENT = "Producer registered: '{}'"
        const val PRODUCER_ALREADY_REGISTERED = "Consumer is already registered. Skipping... id={}"
        const val CLOSE_PRODUCER_EVENT = "Closing kafka producers"
    }
}
