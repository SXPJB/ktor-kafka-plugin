package com.fsociety.ktor.kafka.plugin

import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer
import com.fsociety.ktor.kafka.core.consumer.manager.KtorKafkaConsumerManager
import com.fsociety.ktor.kafka.core.producer.KtorKafkaProducer
import com.fsociety.ktor.kafka.core.registration.KafkaRegistrationHandler
import com.fsociety.ktor.kafka.core.registration.manager.KtorKafkaProducerManager
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import com.fsociety.ktor.kafka.plugin.config.KtorKafkaPluginConfiguration.Builder as KtorKafkaPluginBuilder

class KtorKafkaPlugin(
    private val job: Job = Job(),
    private val consumerManager: KtorKafkaConsumerManager = KtorKafkaConsumerManager(),
    private val producerManager: KtorKafkaProducerManager = KtorKafkaProducerManager(),
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun start() {
        consumerManager.startAll(this)
    }

    fun stop() {
        consumerManager.stopAll()
        producerManager.closeAll()
    }

    fun <K, V> addConsumer(
        id: String,
        consumer: KtorKafkaConsumer<K, V>,
        listener: (K, V) -> Unit,
    ) = consumerManager.create(id, consumer, listener)

    fun <K, V> addProducer(
        id: String,
        producer: KtorKafkaProducer<K, V>,
    ) {
        producerManager.create(id, producer)
    }

    companion object Plugin :
        BaseApplicationPlugin<Application, KtorKafkaPluginBuilder, KtorKafkaPlugin> {
        override val key: AttributeKey<KtorKafkaPlugin>
            get() = AttributeKey<KtorKafkaPlugin>(KtorKafkaPlugin::class.java.canonicalName)

        override fun install(
            pipeline: Application,
            configure: KtorKafkaPluginBuilder.() -> Unit,
        ): KtorKafkaPlugin {
            val config = KtorKafkaPluginBuilder(pipeline)
                .apply(configure)
                .build()

            val plugin = KtorKafkaPlugin()
            val registry = KafkaRegistrationHandler(plugin)

            config.getKafkaRegistration().forEach { registry.handle(it) }

            pipeline.monitor.subscribe(ApplicationStarted) {
                plugin.start()
            }

            pipeline.monitor.subscribe(ApplicationStopping) {
                plugin.stop()
            }

            return plugin
        }
    }
}
