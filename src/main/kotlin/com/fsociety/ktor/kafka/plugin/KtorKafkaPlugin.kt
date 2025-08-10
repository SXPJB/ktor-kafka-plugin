package com.fsociety.ktor.kafka.plugin

import com.fsociety.ktor.kafka.config.KtorKafkaPluginConfiguration
import com.fsociety.ktor.kafka.consumer.manager.KtorKafkaConsumerManager
import com.fsociety.ktor.kafka.shared.KtorKafkaConsumerSpec
import com.fsociety.ktor.kafka.utils.logger
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private const val KTOR_KAFKA_PLUGIN_NAME = "KtorKafkaPlugin"
private const val KTOR_KAFKA_CONFIG_PATH = "ktor.kafka"
private const val APP_STOPPED_EVENT = "Application stopped: Kafka consumers have been requested to stop"

/**
 * A plugin for managing Kafka consumers within a Ktor application.
 *
 * This plugin enables the configuration, registration, and management of Kafka consumers in a coroutine-based
 * environment. It allows consumers to be dynamically registered with specific configurations and listeners for
 * processing Kafka messages.
 *
 * The plugin lifecycle integrates with the application's lifecycle:
 * - Upon `ApplicationStarted`, all registered consumers are initialized and started.
 * - Upon `ApplicationStopping`, all consumers are stopped gracefully, ensuring proper cleanup.
 *
 * Consumers can be configured through the plugin configuration using the `consumer` method, which accepts a
 * consumer-specific builder for setting options like topics, deserializers, and message listeners.
 *
 * The plugin internally utilizes `KtorKafkaConsumerManager` for handling consumer lifecycle operations and concurrency.
 */
val KtorKafkaPlugin = createApplicationPlugin(
    name = KTOR_KAFKA_PLUGIN_NAME,
    configurationPath = KTOR_KAFKA_CONFIG_PATH,
    createConfiguration = ::KtorKafkaPluginConfiguration,
) {
    val log = logger()
    val consumerManager = KtorKafkaConsumerManager(
        scope = CoroutineScope(Dispatchers.IO),
    )

    on(MonitoringEvent(ApplicationStarted)) {
        pluginConfig.registrations.forEach { registration ->
            when (registration) {
                is KtorKafkaConsumerSpec<*, *> -> {
                    consumerManager.register(registration)
                }
            }
        }
        consumerManager.start()
    }

    on(MonitoringEvent(ApplicationStopping)) {
        runBlocking {
            consumerManager.stop()
        }
        log.info(APP_STOPPED_EVENT)
    }
}
