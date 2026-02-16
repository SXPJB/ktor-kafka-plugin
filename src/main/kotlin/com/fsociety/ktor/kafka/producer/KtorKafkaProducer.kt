package com.fsociety.ktor.kafka.producer

import com.fsociety.ktor.kafka.utils.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.atomic.AtomicBoolean

typealias ProducerCallback = (metadata: RecordMetadata, exception: Exception?) -> Unit

class KtorKafkaProducer<K, V>(
    private val kafkaProducer: KafkaProducer<K, V>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val log = logger()
    private var closed = AtomicBoolean(false)
    private var lastSendJob: Job? = null
    private val defaultCallback: ProducerCallback = { metadata, exception ->
        if (exception != null) {
            log.error(SEND_SUCCESS_LOG_FORMAT.format(exception.message), exception)
        } else {
            log.debug(SEND_ERROR_LOG_FORMAT.format(metadata.topic(), metadata.partition(), metadata.offset()))
        }
    }

    fun send(
        topic: String,
        message: V,
        onCompletion: ProducerCallback = defaultCallback
    ) {
        val record = ProducerRecord<K, V>(topic, message)
        send(record, onCompletion)
    }

    fun send(
        topic: String,
        message: V,
        key: K,
        onCompletion: ProducerCallback = defaultCallback
    ) {
        val record = ProducerRecord(topic, key, message)
        send(record, onCompletion)
    }

    fun send(
        record: ProducerRecord<K, V>,
        onCompletion: ProducerCallback = defaultCallback
    ) {
        if (closed.get()) {
            log.warn(PRODUCER_CLOSED_CANNOT_SEND)
            return
        }
        lastSendJob = scope.launch {
            try {
                kafkaProducer.send(record, onCompletion)
            } catch (e: Exception) {
                log.error(UNEXPECTED_ERROR_SENDING_RECORD, e)
            }
        }
    }

    fun close() {
        if (closed.compareAndSet(false, true)) {
            try {
                lastSendJob?.cancel()
            } catch (e: Exception) {
                log.error(UNEXPECTED_ERROR_CLOSING_PRODUCER, e)
            } finally {
                kafkaProducer.close()
                log.debug(PRODUCER_CLOSED_SUCCESSFULLY)
            }
        }
    }

    private companion object {
        const val PRODUCER_CLOSED_CANNOT_SEND = "Producer is closed. Cannot send message."
        const val PRODUCER_CLOSED_SUCCESSFULLY = "Producer closed."
        const val SEND_SUCCESS_LOG_FORMAT = "Message sent to topic: '%s', partition: '%d', offset: '%d'"
        const val SEND_ERROR_LOG_FORMAT = "Error sending message: '%s'"
        const val UNEXPECTED_ERROR_SENDING_RECORD = "An error occurred while processing Kafka messages."
        const val UNEXPECTED_ERROR_CLOSING_PRODUCER = "An error occurred while closing the Kafka producer."
    }
}
