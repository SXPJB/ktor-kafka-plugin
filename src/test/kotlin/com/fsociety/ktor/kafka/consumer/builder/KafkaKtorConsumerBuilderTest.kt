package com.fsociety.ktor.kafka.consumer.builder

import com.fsociety.ktor.kafka.config.KtorKafkaConsumerConfig
import com.fsociety.ktor.kafka.shared.KtorKafkaConsumerSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.types.shouldBeInstanceOf
import org.apache.kafka.common.serialization.StringDeserializer

class KafkaKtorConsumerBuilderTest : DescribeSpec({
    describe("KafkaKtorConsumerBuilder validation") {
        it("fails when listener is not provided") {
            val builder = KafkaKtorConsumerBuilder<String, String>(KtorKafkaConsumerConfig<String, String>())
            builder.configure {
                bootstrapServers = "localhost:9092"
                groupId = "group"
                topics = listOf("topic")
                keyDeserializer = StringDeserializer::class
                valueDeserializer = StringDeserializer::class
            }

            val ex = shouldThrow<IllegalArgumentException> { builder.build() }
            ex.message!!.contains("Kafka consumer listener can not be null").shouldBeTrue()
        }

        it("fails when bootstrap servers are missing") {
            val builder = KafkaKtorConsumerBuilder<String, String>(KtorKafkaConsumerConfig<String, String>())
            builder.configure {
                groupId = "group"
                topics = listOf("topic")
                keyDeserializer = StringDeserializer::class
                valueDeserializer = StringDeserializer::class
            }

            shouldThrow<IllegalArgumentException> { builder.build() }
                .message!!.contains("Missing bootstrap servers configuration.").shouldBeTrue()
        }

        it("fails when group id is missing") {
            val builder = KafkaKtorConsumerBuilder<String, String>(KtorKafkaConsumerConfig<String, String>())
            builder.configure {
                bootstrapServers = "localhost:9092"
                topics = listOf("topic")
                keyDeserializer = StringDeserializer::class
                valueDeserializer = StringDeserializer::class
            }

            shouldThrow<IllegalArgumentException> { builder.build() }
                .message!!.contains("Missing consumer group ID.").shouldBeTrue()
        }

        it("fails when topics are empty") {
            val builder = KafkaKtorConsumerBuilder<String, String>(KtorKafkaConsumerConfig<String, String>())
            builder.configure {
                bootstrapServers = "localhost:9092"
                groupId = "group"
                keyDeserializer = StringDeserializer::class
                valueDeserializer = StringDeserializer::class
                topics = emptyList()
            }

            shouldThrow<IllegalArgumentException> { builder.build() }
                .message!!.contains("No topics specified.").shouldBeTrue()
        }

        it("fails when deserializers are missing") {
            val builder = KafkaKtorConsumerBuilder<String, String>(KtorKafkaConsumerConfig<String, String>())
            builder.configure {
                bootstrapServers = "localhost:9092"
                groupId = "group"
                topics = listOf("topic")
            }

            shouldThrow<IllegalArgumentException> { builder.build() }
                .message!!.contains("Missing key deserializer.").shouldBeTrue()
        }

        it("builds a consumer spec with valid configuration and listener") {
            val builder = KafkaKtorConsumerBuilder<String, String>(KtorKafkaConsumerConfig<String, String>())
            builder.configure {
                bootstrapServers = "localhost:9092"
                groupId = "group"
                topics = listOf("topic")
                keyDeserializer = StringDeserializer::class
                valueDeserializer = StringDeserializer::class
            }
            builder.listener { _, _ -> }

            val spec = builder.build()
            spec.shouldBeInstanceOf<KtorKafkaConsumerSpec<String, String>>()
        }
    }
})
