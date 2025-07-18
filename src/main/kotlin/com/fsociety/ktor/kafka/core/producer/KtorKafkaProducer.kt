package com.fsociety.ktor.kafka.core.producer

import com.fsociety.ktor.kafka.common.utils.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.atomic.AtomicBoolean

class KtorKafkaProducer<K, V>(
    private val kafkaProducer: Producer<K, V>,
    private val topic: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val logger = logger()
    private val isClose = AtomicBoolean(false)

    fun send(message: V) {
        val record = ProducerRecord<K, V>(topic, message)
        send(record)
    }

    fun send(message: V, key: K) {
        val record = ProducerRecord(topic, key, message)
        send(record)
    }

    fun send(record: ProducerRecord<K, V>) {
        send(record) { metadata, exception ->
            if (exception != null) {
                logger.error(ERROR_SENDING_MESSAGE_EVENT, exception)
            } else {
                logger.debug(
                    PRODUCER_SENT_MESSAGE_EVENT,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                )
            }
        }
    }

    fun send(record: ProducerRecord<K, V>, callback: (metadata: RecordMetadata, exception: Exception?) -> Unit) {
        if (!isClose.compareAndSet(false, true)) {
            logger.warn(PRODUCER_IS_CLOSE_EVENT)
            return
        }
        scope.launch {
            runCatching {
                kafkaProducer.send(record, callback)
            }.onFailure {
                logger.error(ERROR_SENDING_MESSAGE_EVENT, it)
            }
        }
    }

    fun close() {
        isClose.set(true)
        kafkaProducer.close()
        scope.cancel()
    }

    companion object {
        private const val PRODUCER_IS_CLOSE_EVENT = "Producer is closed. Cannot send message."
        private const val PRODUCER_SENT_MESSAGE_EVENT = "Message sent to topic: '{}', partition:'{}', offset: '{}'"
        private const val ERROR_SENDING_MESSAGE_EVENT = "Error while sending kafka message."
    }
}
