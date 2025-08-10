package com.fsociety.ktor.kafka.consumer

import com.fsociety.ktor.kafka.utils.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.Consumer
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A wrapper around Kafka's Consumer designed for integration with Ktor and Kotlin coroutines.
 * Provides functionality to consume messages from the specified Kafka topics in a non-blocking manner.
 *
 * @author Emmanuel H. Ramirez (@sxpjb)
 *
 * @param K The type of value that the kafka Consumer will consume.
 * @param V The type of values that the Kafka Consumer will consume.
 * @property kafkaConsumer The underlying Kafka consumer instance to use for consuming messages.
 * @property topics A list of Kafka topic names to subscribe to.
 * @property scope The coroutine scope on which the Kafka consumer will operate. Defaults to `Dispatchers.IO`.
 */
class KtorKafkaConsumer<K, V>(
    private val kafkaConsumer: Consumer<K, V>,
    private val topics: List<String>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val log = logger()
    private val isRunning = AtomicBoolean(false)
    private var consumerJob: Job? = null

    fun startListening(listener: (K, V) -> Unit) {
        if (!isRunning.compareAndSet(false, true)) {
            log.info(CONSUMER_ALREADY_RUNNING_EVENT, topics)
            return
        }
        kafkaConsumer.subscribe(topics)
        consumerJob = scope.launch {
            try {
                processRecords(listener)
            } catch (e: Exception) {
                log.error(ERROR_CONSUMING_MESSAGES_EVENT, e.message, e)
            } finally {
                cleanup()
            }
        }
    }

    fun stopListening() {
        isRunning.set(false)
        consumerJob?.cancel()
        log.info(STOP_CONSUMING_MESSAGES_EVENT)
    }

    private fun processRecords(listener: (K, V) -> Unit) {
        while (isRunning.get()) {
            val records = kafkaConsumer.poll(POLL_DURATION)
            for (record in records) {
                listener(record.key(), record.value())
            }
        }
    }

    private fun cleanup() {
        kafkaConsumer.close()
        isRunning.set(false)
    }

    companion object {
        private const val CONSUMER_ALREADY_RUNNING_EVENT = "Kafka consumer is already running for topics: {}"
        private const val ERROR_CONSUMING_MESSAGES_EVENT = "Failed to process Kafka messages: {}"
        private const val STOP_CONSUMING_MESSAGES_EVENT = "Stopping Kafka consumer and closing connection"
        private val POLL_DURATION = Duration.ofMillis(100L)
    }
}
