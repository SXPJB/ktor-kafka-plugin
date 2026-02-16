package com.fsociety.ktor.kafka.producer.builder

import com.fsociety.ktor.kafka.config.KtorKafkaConfig
import com.fsociety.ktor.kafka.config.KtorKafkaProducerConfig
import com.fsociety.ktor.kafka.producer.KtorKafkaProducer
import com.fsociety.ktor.kafka.shared.KtorKafkaProducerSpec
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.Properties

class KafkaKtorProducerBuilder<K, V>(
    private val pluginConfig: KtorKafkaConfig,
) {
    private var config: KtorKafkaProducerConfig<K, V> = KtorKafkaProducerConfig()

    fun configure(block: KtorKafkaProducerConfig<K, V>.() -> Unit) {
        config = config.copy().apply(block)
    }

    fun property(props: Map<String, Any>) {
        config = config.copy(extraProperties = config.extraProperties + props)
    }

    fun property(vararg props: Pair<String, Any>) {
        property(props.toMap())
    }

    internal fun build(): KtorKafkaProducerSpec<K, V> {
        config = requireValidConfig()
        return KtorKafkaProducerSpec(
            id = config.id,
            ktorKafkaProducer = KtorKafkaProducer(
                kafkaProducer = createKafkaProducer(),
            )
        )
    }

    private fun requireValidConfig(): KtorKafkaProducerConfig<K, V> {
        val finalConfig = config.copy(
            bootstrapServers = pluginConfig.bootstrapServers ?: config.bootstrapServers,
            groupId = pluginConfig.groupId ?: config.groupId,
        )

        validateConfigurationParameters(finalConfig)

        return finalConfig
    }

    private fun validateConfigurationParameters(config: KtorKafkaProducerConfig<K, V>) {
        with(config) {
            requireNotNull(bootstrapServers) { ERROR_MISSING_BOOTSTRAP_SERVERS }
            requireNotNull(groupId) { ERROR_MISSING_GROUP_ID }
            requireNotNull(topic) { ERROR_NO_TOPIC }
            requireNotNull(keySerializer) { ERROR_MISSING_KEY_SERIALIZER }
            requireNotNull(valueSerializer) { ERROR_MISSING_VALUE_SERIALIZER }
        }
    }

    private fun createKafkaProducer(): KafkaProducer<K, V> {
        return Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.keySerializer?.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.valueSerializer?.java)
            putAll(config.extraProperties)
        }.let { KafkaProducer(it) }
    }

    private companion object {
        const val ERROR_MISSING_BOOTSTRAP_SERVERS = "Missing bootstrap servers configuration."
        const val ERROR_MISSING_GROUP_ID = "Missing consumer group ID."
        const val ERROR_NO_TOPIC = "No topic specified."
        const val ERROR_MISSING_KEY_SERIALIZER = "Missing key serializer."
        const val ERROR_MISSING_VALUE_SERIALIZER = "Missing value serializer."
    }
}
