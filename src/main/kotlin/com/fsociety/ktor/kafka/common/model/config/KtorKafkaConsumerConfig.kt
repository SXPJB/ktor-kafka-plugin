package com.fsociety.ktor.kafka.common.model.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import java.util.UUID
import kotlin.reflect.KClass

sealed class KtorKafkaConfig<K, V>(
    open var bootstrapServers: String?,
    open var groupId: String?,
)

data class KtorKafkaConsumerConfig<K, V>(
    var id: String = UUID.randomUUID().toString(),
    override var bootstrapServers: String? = null,
    override var groupId: String? = null,
    var keyDeserializer: KClass<out Deserializer<K>>? = null,
    var valueDeserializer: KClass<out Deserializer<V>>? = null,
    var topics: List<String> = emptyList(),
    var extraProperties: Map<ConsumerConfig, Any> = emptyMap(),
) : KtorKafkaConfig<K, V>(
    bootstrapServers = bootstrapServers,
    groupId = groupId,
)
