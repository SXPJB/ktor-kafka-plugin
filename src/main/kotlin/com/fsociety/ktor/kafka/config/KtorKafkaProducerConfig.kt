package com.fsociety.ktor.kafka.config

import org.apache.kafka.common.serialization.Serializer
import java.util.UUID
import kotlin.reflect.KClass

data class KtorKafkaProducerConfig<K, V>(
    var id: String = UUID.randomUUID().toString(),
    override var bootstrapServers: String? = null,
    override var groupId: String? = null,
    var keySerializer: KClass<out Serializer<K>>? = null,
    var valueSerializer: KClass<out Serializer<V>>? = null,
    var topic: String? = null,
    var extraProperties: Map<String, Any> = emptyMap(),
) : KtorKafkaConfig(
    bootstrapServers = bootstrapServers,
    groupId = groupId,
)
