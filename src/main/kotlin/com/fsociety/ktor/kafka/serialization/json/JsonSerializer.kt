package com.fsociety.ktor.kafka.serialization.json

import com.fsociety.ktor.kafka.common.utils.logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.apache.kafka.common.serialization.Serializer

private const val ERROR_SERIALIZATION_EVENT = "Json can't be deserialized"

/**
 * A serializer implementation that serializes objects of type [T] into JSON format using kotlinx.serialization.
 *
 * @param T The target type that this serializer converts into JSON.
 * @property serializer The [KSerializer] instance used to serialize the object into JSON.
 * @property json The [Json] instance used for JSON encoding, with a default configuration using snake_case naming strategy.
 *
 * This class handles serialization of data, converting objects to JSON byte arrays. It includes error handling
 * to log serialization failures, ensuring robustness during the conversion process.
 */
@OptIn(ExperimentalSerializationApi::class)
open class JsonSerializer<T>(
    private val serializer: KSerializer<T>,
    private val json: Json = Json {
        namingStrategy = JsonNamingStrategy.Builtins.SnakeCase
    },
) : Serializer<T> {

    private val log = logger()

    override fun serialize(topic: String, data: T): ByteArray {
        return runCatching { encode(data) }.getOrElse {
            log.error(ERROR_SERIALIZATION_EVENT, it)
            throw IllegalArgumentException("Can't serialize $data")
        }
    }

    private fun encode(data: T): ByteArray {
        return json.encodeToString(serializer, data)
            .toByteArray(Charsets.UTF_8)
    }
}
