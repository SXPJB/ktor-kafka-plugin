package com.fsociety.ktor.kafka.serialization.json

import com.fsociety.ktor.kafka.utils.logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.apache.kafka.common.serialization.Deserializer

private const val ERROR_DESERIALIZATION_EVENT = "Json can't be deserialized"

/**
 * A deserializer implementation that deserializes JSON data into objects of type [T] using kotlinx.serialization.
 *
 * @param T The target type this deserializer converts JSON data into.
 * @property serializer The [KSerializer] instance used to deserialize the JSON data.
 * @property json The [Json] instance used for parsing JSON, with a default configuration using snake_case naming strategy.
 *
 * @author Emmanuel H. Ramirez (@sxpjb)
 *
 * This class handles deserialization of incoming message data by converting from a JSON byte array
 * into objects of the specified type. It leverages error handling to log failures during deserialization,
 * ensuring safe and manageable parsing.
 */
@OptIn(ExperimentalSerializationApi::class)
open class JsonDeserializer<T>(
    private val serializer: KSerializer<T>,
    val json: Json = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
    },
) : Deserializer<T> {

    private val log = logger()

    override fun deserialize(topic: String?, data: ByteArray?): T? {
        return runCatching {
            data?.let { decode(data) }
        }.onFailure {
            log.error(ERROR_DESERIALIZATION_EVENT, it)
        }.getOrNull()
    }

    private fun decode(data: ByteArray): T? {
        val dataString = data.toString(Charsets.UTF_8)
        return json.decodeFromString(serializer, dataString)
    }
}
