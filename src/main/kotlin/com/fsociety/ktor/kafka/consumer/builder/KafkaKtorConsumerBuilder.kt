package com.fsociety.ktor.kafka.consumer.builder

import com.fsociety.ktor.kafka.config.KtorKafkaConfig
import com.fsociety.ktor.kafka.config.KtorKafkaConsumerConfig
import com.fsociety.ktor.kafka.consumer.KtorKafkaConsumer
import com.fsociety.ktor.kafka.shared.KtorKafkaConsumerSpec
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.Properties

/**
 * Builder class for creating and configuring a Kafka consumer using the Ktor Kafka integration framework.
 *
 * @param K The type of keys in Kafka messages.
 * @param V The type of values in Kafka messages.
 * @constructor Initializes the builder with the given `KtorKafkaConfig`.
 *
 * @property pluginConfig A configuration object that provides fallback values for bootstrap servers
 * and group ID if not explicitly set in the consumer configuration.
 */
class KafkaKtorConsumerBuilder<K, V>(
    private val pluginConfig: KtorKafkaConfig,
) {
    private var config: KtorKafkaConsumerConfig<K, V> = KtorKafkaConsumerConfig()
    private var listener: ((K, V) -> Unit)? = null

    fun configure(block: KtorKafkaConsumerConfig<K, V>.() -> Unit) {
        config = config.copy().apply(block)
    }

    fun listener(listener: (K, V) -> Unit) {
        this.listener = listener
    }

    fun property(props: Map<String, Any>) {
        config = config.copy(extraProperties = config.extraProperties + props)
    }

    fun property(vararg pairs: Pair<String, Any>) {
        property(pairs.toMap())
    }

    internal fun build(): KtorKafkaConsumerSpec<K, V> {
        config = requireValidConfig()

        return KtorKafkaConsumerSpec(
            id = config.id,
            listener = requireNotNull(listener) { LISTENER_NULL_MESSAGE },
            ktorKafkaConsumer = KtorKafkaConsumer(
                kafkaConsumer = createKafkaConsumer(),
                topics = config.topics,
            ),
        )
    }

    private fun requireValidConfig(): KtorKafkaConsumerConfig<K, V> {
        val finalConfig = config.copy(
            bootstrapServers = pluginConfig.bootstrapServers ?: config.bootstrapServers,
            groupId = pluginConfig.groupId ?: config.groupId,
        )

        validateConfigurationParameters(finalConfig)

        return finalConfig
    }

    private fun validateConfigurationParameters(config: KtorKafkaConsumerConfig<K, V>) {
        with(config) {
            requireNotNull(bootstrapServers) { ERROR_MISSING_BOOTSTRAP_SERVERS }
            requireNotNull(groupId) { ERROR_MISSING_GROUP_ID }

            require(topics.isNotEmpty()) { ERROR_NO_TOPICS }

            requireNotNull(keyDeserializer) { ERROR_MISSING_KEY_DESERIALIZER }
            requireNotNull(valueDeserializer) { ERROR_MISSING_VALUE_DESERIALIZER }
        }
    }

    private fun createKafkaConsumer(): Consumer<K, V> {
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.keyDeserializer?.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.valueDeserializer?.java)
            putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            putAll(config.extraProperties)
        }.let { KafkaConsumer(it) }
    }

    private companion object {
        const val ERROR_MISSING_BOOTSTRAP_SERVERS = "Missing bootstrap servers configuration."
        const val ERROR_MISSING_GROUP_ID = "Missing consumer group ID."
        const val ERROR_NO_TOPICS = "No topics specified."
        const val ERROR_MISSING_KEY_DESERIALIZER = "Missing key deserializer."
        const val ERROR_MISSING_VALUE_DESERIALIZER = "Missing value deserializer."
        private const val LISTENER_NULL_MESSAGE = "Kafka consumer listener can not be null"
    }
}
