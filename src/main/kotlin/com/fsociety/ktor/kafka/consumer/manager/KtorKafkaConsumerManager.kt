package com.fsociety.ktor.kafka.consumer.manager

import com.fsociety.ktor.kafka.consumer.model.KtorKafkaConsumerWrapper
import com.fsociety.ktor.kafka.shared.KtorKafkaConsumerSpec
import com.fsociety.ktor.kafka.utils.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages the lifecycle and registration of multiple Kafka consumers in a coroutine-based environment.
 * Handles starting and stopping of all registered consumers, ensuring proper cleanup and concurrency control.
 *
 * @property scope The coroutine scope in which the Kafka consumers operate.
 */
class KtorKafkaConsumerManager(
    private val scope: CoroutineScope,
) {
    private val log = logger()

    private val consumers = ConcurrentHashMap<String, KtorKafkaConsumerWrapper<*, *>>()
    private val jobs = ConcurrentHashMap<String, Job>()

    private val isStopped = AtomicBoolean(false)

    fun <K, V> register(entry: KtorKafkaConsumerSpec<K, V>) {
        val (id) = entry
        if (consumers.putIfAbsent(id, entry.toWrapper()) == null) {
            log.info(LOG_CONSUMER_REGISTERED, id)
        } else {
            log.warn(LOG_CONSUMER_ALREADY_REGISTERED, id)
        }
    }

    fun start() {
        log.info(LOG_STARTING_ALL_CONSUMERS)
        consumers.forEach { (id, wrapper) ->
            jobs.computeIfAbsent(id) {
                log.info(LOG_STARTING_CONSUMER, id)
                scope.launch { wrapper.startListening() }
            }
        }
    }

    suspend fun stop() {
        if (!isStopped.compareAndSet(false, true)) {
            log.info(LOG_STOP_REQUEST_IGNORED_ALREADY_STOPPING)
            return
        }

        log.info(LOG_STOPPING_ALL_CONSUMERS)
        consumers.forEach { (id, wrapper) ->
            log.info(LOG_STOPPING_CONSUMER, id)
            wrapper.stopListening()
        }

        jobs.forEach { (id, job) ->
            runCatching {
                job.cancelAndJoin()
                log.info(LOG_CONSUMER_JOB_FINISHED, id)
            }.onFailure { e ->
                log.warn(LOG_ERROR_STOPPING_CONSUMER_JOB, id, e.message)
            }
        }

        jobs.clear()
    }

    private fun <K, V> KtorKafkaConsumerSpec<K, V>.toWrapper(): KtorKafkaConsumerWrapper<K, V> {
        return KtorKafkaConsumerWrapper(
            ktorKafkaConsumer,
            listener,
        )
    }

    private companion object {
        const val LOG_CONSUMER_REGISTERED = "Consumer registered: {}"
        const val LOG_CONSUMER_ALREADY_REGISTERED = "Consumer is already registered. Skipping... id={}"
        const val LOG_STARTING_ALL_CONSUMERS = "Starting Kafka consumers"
        const val LOG_STARTING_CONSUMER = "Starting Kafka consumer with id: {}"
        const val LOG_STOP_REQUEST_IGNORED_ALREADY_STOPPING =
            "Stop requested but consumers are already stopping/stopped. Ignoring duplicate request."
        const val LOG_STOPPING_ALL_CONSUMERS = "Stopping Kafka consumers"
        const val LOG_STOPPING_CONSUMER = "Stopping Kafka consumer with id: {}"
        const val LOG_CONSUMER_JOB_FINISHED = "Kafka consumer job finished: {}"
        const val LOG_ERROR_STOPPING_CONSUMER_JOB = "Error while stopping consumer job: id={}, reason={}"
    }
}
