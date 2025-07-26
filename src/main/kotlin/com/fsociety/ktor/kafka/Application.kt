package com.fsociety.ktor.kafka

import com.fsociety.ktor.kafka.common.utils.logger
import com.fsociety.ktor.kafka.plugin.KtorKafkaPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.kafka.common.serialization.StringDeserializer

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(KtorKafkaPlugin) {
            bootstrapServers = "localhost:9093"
            groupId = "ktor-group-id"

            consumer {
                configure {
                    valueDeserializer = StringDeserializer::class
                    keyDeserializer = StringDeserializer::class
                    topics = listOf("ktor-topics")
                }
                listener { _, value ->
                    logger().info("Received from $value")
                }
            }
        }
    }.start(wait = true)
}
