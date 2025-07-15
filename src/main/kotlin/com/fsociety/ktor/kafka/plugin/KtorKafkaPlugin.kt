package com.fsociety.ktor.kafka.plugin

import com.fsociety.ktor.kafka.core.consumer.KtorKafkaConsumer
import com.fsociety.ktor.kafka.core.consumer.manager.KtorKafkaConsumerManager
import com.fsociety.ktor.kafka.core.registration.KafkaRegistrationHandler
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

class KtorKafkaPlugin : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val consumerManager = KtorKafkaConsumerManager()

    fun start() {
        consumerManager.startAll(this)
    }

    fun stop() {
        consumerManager.stopAll()
    }

    fun <K, V> addConsumer(
        id: String,
        consumer: KtorKafkaConsumer<K, V>,
        listener: (K, V) -> Unit,
    ) = consumerManager.create(id, consumer, listener)

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
