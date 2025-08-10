package com.fsociety.ktor.kafka.consumer.manager

import com.fsociety.ktor.kafka.consumer.KtorKafkaConsumer
import com.fsociety.ktor.kafka.shared.KtorKafkaConsumerSpec
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class KtorKafkaConsumerManagerTest : DescribeSpec({
    describe("KtorKafkaConsumerManager lifecycle") {
        it("registers consumers, starts them once and stops them idempotently") {
            val scope = CoroutineScope(Dispatchers.Unconfined)
            val manager = KtorKafkaConsumerManager(scope)

            val consumer = mockk<KtorKafkaConsumer<String, String>>(relaxed = true)
            val listener: (String, String) -> Unit = { _, _ -> }
            val spec = KtorKafkaConsumerSpec(
                id = "consumer-1",
                ktorKafkaConsumer = consumer,
                listener = listener,
            )

            manager.register(spec)
            manager.register(spec) // duplicate register should be ignored

            manager.start()
            // give coroutine a chance to schedule
            Thread.sleep(10)

            verify(exactly = 1) { consumer.startListening(any()) }

            runBlocking {
                manager.stop()
                manager.stop() // duplicate stop should be ignored
            }

            verify(exactly = 1) { consumer.stopListening() }
        }
    }
})
