# Ktor Kafka Plugin

A Ktor plugin for seamless integration with Apache Kafka, allowing you to easily consume Kafka messages within your Ktor application.

## Overview

The Ktor Kafka Plugin provides a simple and idiomatic way to integrate Kafka consumers into your Ktor application. It handles the lifecycle of Kafka consumers, automatically starting them when your application starts and gracefully shutting them down when your application stops.

## Features

- 🚀 **Simple Integration**: Easy to install and configure within your Ktor application
- 🔄 **Lifecycle Management**: Automatically manages the lifecycle of Kafka consumers
- 🧩 **Type-Safe Configuration**: Fully type-safe DSL for configuring Kafka consumers
- 🔌 **Multiple Consumers**: Support for multiple Kafka consumers with different configurations
- 🧵 **Coroutine Support**: Built with Kotlin coroutines for non-blocking message processing

### What’s new (latest refactor)
- Refactored package structure for clearer separation of concerns (config, consumer, plugin, utils)
- New configuration classes: KtorKafkaConfig, KtorKafkaPluginConfiguration, KtorKafkaConsumerConfig
- New consumer management: KtorKafkaConsumerManager with KtorKafkaConsumerWrapper
- Simplified registration via DSL (consumer { configure { ... } listener { ... } })
- Removed legacy registration/manager classes; updated plugin wiring
- JSON Serializer/Deserializer utilities improved (kotlinx.serialization based)
- Added shared specs and unit tests to cover builder, manager, and plugin lifecycle

## Installation

Add the dependency to your `build.gradle.kts` file:

```kotlin
implementation("com.fsociety.ktor:ktor-kafka-plugin:0.0.1-ALPHA")
```
No release yet; coming soon.

## Usage

### Basic Setup

Here's a simple example of how to use the Ktor Kafka Plugin:

> **Note:** You'll need to import the following classes:
> - `com.fsociety.ktor.kafka.plugin.KtorKafkaPlugin`
> - `io.ktor.server.application.install`
> - `io.ktor.server.engine.embeddedServer`
> - `io.ktor.server.netty.Netty`
> - `org.apache.kafka.common.serialization.StringDeserializer`

```kotlin
fun main() {
    embeddedServer(Netty, port = 8080) {
        // Install the Ktor Kafka Plugin
        install(KtorKafkaPlugin) {
            // Configure Kafka connection
            bootstrapServers = "localhost:9092"
            groupId = "my-consumer-group"

            // Configure a consumer
            consumer {
                configure {
                    topics = listOf("my-topic")
                    valueDeserializer = StringDeserializer::class
                    keyDeserializer = StringDeserializer::class
                }

                // Define what happens when a message is received
                listener { key, value ->
                    log.info("Received message: $key = $value")
                    // Process your message here
                }
            }
        }

        // Rest of your Ktor application
        routing {
            get("/") {
                call.respondText("Hello, world!")
            }
        }
    }.start(wait = true)
}
```

### Advanced Configuration

You can configure multiple consumers with different settings:

> **Note:** In addition to the imports from the basic example, you'll need:
> - `org.apache.kafka.clients.consumer.ConsumerConfig`
> - `com.fsociety.ktor.kafka.serialization.json.JsonDeserializer`
> - `com.fsociety.ktor.kafka.serialization.json.JsonSerializer`
> - `kotlinx.serialization.Serializable`
> - `kotlinx.serialization.serializer`

```kotlin
// First, define your data classes with custom serializers/deserializers
@Serializable
data class MyMessage(
    val id: String,
    val content: String,
    val timestamp: Long
) {
    // Custom deserializer for MyMessage
    class MyMessageDeserializer : JsonDeserializer<MyMessage>(serializer())

    // Custom serializer for MyMessage
    class MyMessageSerializer : JsonSerializer<MyMessage>(serializer())
}

install(KtorKafkaPlugin) {
    bootstrapServers = "localhost:9092"

    // First consumer for string messages
    consumer {
        configure {
            id = "string-consumer"  // Optional: provide a custom ID
            groupId = "string-group"
            topics = listOf("string-topic")
            valueDeserializer = StringDeserializer::class
            keyDeserializer = StringDeserializer::class
        }

        listener { key, value ->
            log.info("String message: $key = $value")
        }
    }

    // Second consumer for JSON messages
    consumer {
        configure {
            id = "json-consumer"
            groupId = "json-group"
            topics = listOf("json-topic")
            valueDeserializer = MyMessage.MyMessageDeserializer::class
            keyDeserializer = StringDeserializer::class

            // Set additional Kafka consumer properties
            extraProperties = mapOf(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 100
            )
        }

        listener { key, value ->
            log.info("JSON message: $key = ${value.content}")
        }
    }
}
```

The `extraProperties` map allows you to set any additional Kafka consumer configuration properties beyond the standard ones provided by the plugin. This gives you full control over the underlying Kafka consumer configuration, enabling you to fine-tune its behavior according to your specific requirements. Use the constants from `ConsumerConfig` as keys for type-safe configuration.

## Configuration Options

### Plugin Level

| Option            | Description                                   | Required |
|-------------------|-----------------------------------------------|----------|
| bootstrapServers  | Kafka broker addresses (comma-separated list) | Yes      |
| groupId           | Default consumer group ID                     | Yes      |

### Configuration via application.conf or application.yml

You can configure the plugin using your application's configuration files instead of code:

#### application.conf

```hocon
ktor {
  kafka {
    bootstrap_servers = "localhost:9092"
    groupId = "my-consumer-group"
  }
}
```

#### application.yml

```yaml
ktor:
  kafka:
    bootstrap_servers: "localhost:9092"
    groupId: "my-consumer-group"
```

When using configuration files, you can omit these properties in your code as they will be automatically loaded from the configuration.

### Consumer Level

| Option            | Description                                   | Required | Default       |
|-------------------|-----------------------------------------------|----------|---------------|
| id                | Unique identifier for the consumer            | No       | Random UUID   |
| bootstrapServers  | Override the plugin-level setting             | No       | Plugin value  |
| groupId           | Override the plugin-level setting             | No       | Plugin value  |
| topics            | List of topics to subscribe to                | Yes      | -             |
| keyDeserializer   | Class reference for message key deserializer  | Yes      | -             |
| valueDeserializer | Class reference for message value deserializer| Yes      | -             |
| extraProperties   | Map of additional Kafka consumer properties   | No       | Empty map     |
| listener          | Function to process received messages         | Yes      | -             |

## Testing

- Run all tests: `./gradlew test`
- What’s covered:
  - Builder validation and spec creation (KafkaKtorConsumerBuilderTest)
  - Consumer manager lifecycle (KtorKafkaConsumerManagerTest)
  - Plugin install/start/stop hooks (KtorKafkaPluginTest)

If you add new DSL or lifecycle features, please include matching tests to keep coverage healthy.

## Building & Running

To build or run the project, use one of the following tasks:

| Task                | Description                                                          |
|---------------------|----------------------------------------------------------------------|
| `./gradlew test`    | Run the tests                                                        |
| `./gradlew build`   | Build everything                                                     |
| `./gradlew publish` | Publish the library to the configured repository                     |

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
