package com.fsociety.ktor.kafka.common.utils

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Properties

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

fun <K, V> createConsumer(
    bootstrapServers: String,
    groupId: String,
    valueDeserializer: Deserializer<V>,
    keyDeserializer: Deserializer<K>,
): Consumer<K, V> {
    val props = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            keyDeserializer::class.java,
        )
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer::class.java)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
    return KafkaConsumer(props)
}

fun <K, V> createProducer(
    bootstrapServers: String,
    keySerializer: Serializer<K>,
    valueSerializer: Serializer<V>,
    vararg extraProperties: Pair<ProducerConfig, Any>,
): Producer<K, V> {
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer::class.java)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer::class.java)
        extraProperties.forEach {
            put(it.first, it.second)
        }
    }
    return KafkaProducer(props)
}
