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

## Installation

Add the dependency to your `build.gradle.kts` file:

```kotlin
implementation("com.fsociety.ktor:ktor-kafka-plugin:0.0.1-ALPHA")
```

## Usage

### Basic Setup

Here's a simple example of how to use the Ktor Kafka Plugin:

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
                topics = listOf("my-topic")
                valueDeserializer = StringDeserializer()
                keyDeserializer = StringDeserializer()

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

```kotlin
install(KtorKafkaPlugin) {
    bootstrapServers = "localhost:9092"

    // First consumer for string messages
    consumer {
        id = "string-consumer"  // Optional: provide a custom ID
        groupId = "string-group"
        topics = listOf("string-topic")
        valueDeserializer = StringDeserializer()
        keyDeserializer = StringDeserializer()

        listener { key, value ->
            log.info("String message: $key = $value")
        }
    }

    // Second consumer for JSON messages
    consumer {
        id = "json-consumer"
        groupId = "json-group"
        topics = listOf("json-topic")
        valueDeserializer = JsonDeserializer<MyDataClass>()
        keyDeserializer = StringDeserializer()

        listener { key, value ->
            log.info("JSON message: $key = ${value.someProperty}")
        }
    }
}
```

## Configuration Options

### Plugin Level

| Option            | Description                                   | Required |
|-------------------|-----------------------------------------------|----------|
| bootstrapServers  | Kafka broker addresses (comma-separated list) | Yes      |
| groupId           | Default consumer group ID                     | Yes      |

### Consumer Level

| Option            | Description                                   | Required | Default       |
|-------------------|-----------------------------------------------|----------|---------------|
| id                | Unique identifier for the consumer            | No       | Random UUID   |
| bootstrapServers  | Override the plugin-level setting             | No       | Plugin value  |
| groupId           | Override the plugin-level setting             | No       | Plugin value  |
| topics            | List of topics to subscribe to                | Yes      | -             |
| keyDeserializer   | Deserializer for message keys                 | Yes      | -             |
| valueDeserializer | Deserializer for message values               | Yes      | -             |
| listener          | Function to process received messages         | Yes      | -             |

## Testing

For information on how to test Kafka consumers, please refer to the [Testing Guide](src/test/kotlin/com/fsociety/ktor/kafka/core/consumer/TESTING_GUIDE.md).

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
