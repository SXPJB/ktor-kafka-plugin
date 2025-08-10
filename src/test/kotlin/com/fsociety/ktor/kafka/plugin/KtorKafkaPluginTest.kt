package com.fsociety.ktor.kafka.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication

class KtorKafkaPluginTest : DescribeSpec({
    describe("KtorKafkaPlugin lifecycle with no consumers registered") {
        it("installs and handles ApplicationStarted/ApplicationStopping without errors") {
            testApplication {
                application {
                    install(KtorKafkaPlugin)
                }
            }
        }
    }
})
