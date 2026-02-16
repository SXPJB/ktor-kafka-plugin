Ktor Kafka Plugin – Development Guidelines

Audience: Experienced Kotlin/Gradle/Ktor developers contributing to this codebase.

This document captures project-specific build, test, and development practices that will save time and avoid common pitfalls.

1. Build and Configuration

1.1 Toolchain and Plugins
- Gradle: Wrapper is provided; always use ./gradlew.
- Kotlin: 2.1.10 (see gradle.properties: kotlinVersion=2.1.10).
- Ktor: Plugin applied id io.ktor.plugin version 3.2.1. Note: ktorVersion in gradle.properties is 3.2.0; coordinates like io.ktor:ktor-server-core are used without explicit version as they are managed by the Ktor Gradle plugin.
- Kotlinx Serialization: org.jetbrains.kotlin.plugin.serialization 2.1.10.
- Lint/Formatting: org.jlleitschuh.gradle.ktlint 12.3.0 (tasks: ktlintCheck, ktlintFormat).
- Publishing: maven-publish is configured with a sources JAR. A repository is not yet configured (TODO in build.gradle.kts).
- Java: Project applies java-library; Kotlin JVM plugin manages JVM target.

1.2 Coordinates and Naming
- group = com.fsociety.ktor.pluging and rootProject.name = ktor-kafka-pluging. Note the trailing g in pluging appears intentional in this repository but may not be the final artifact coordinates. Align any publication/consumption accordingly, or consider normalizing to plugin if/when cutting a release.

1.3 Dependencies of Interest
- Kafka: org.apache.kafka:kafka-clients and kafka-streams (version from gradle.properties, kafkaVersion=4.0.0). Runtime connections are only established if/when a Consumer is actually polled; unit tests avoid network use.
- DI: Koin 4.1.0 (ktor + slf4j logger).
- Arrow: arrow-core and arrow-fx-coroutines 2.1.2.
- Logging: logback-classic 1.5.13; logback.xml is intentionally minimal. You can augment for local debugging as needed.

1.4 Building
- Full build: ./gradlew build
- Compile only: ./gradlew compileKotlin
- Run tests: ./gradlew test
- Lint: ./gradlew ktlintCheck
- Auto-format: ./gradlew ktlintFormat

1.5 IDE
- .editorconfig configures Kotlin import and indentation rules. Ktlint uses IntelliJ style. Let the IDE respect .editorconfig for consistent formatting.

2. Testing

2.1 Test Framework
- Kotest 5.9.1 with JUnit5 engine (tasks.test { useJUnitPlatform() }).
- Mocking: MockK 1.13.13.
- Ktor: ktor-server-test-host for plugin lifecycle tests (no Netty engine spun up).

2.2 How Existing Tests Are Structured
- src/test/kotlin/com/fsociety/ktor/kafka/consumer/builder/KafkaKtorConsumerBuilderTest.kt
  - Validates builder input checks (bootstrap servers, groupId, topics, deserializers) and listener presence.
  - build() creates an org.apache.kafka.clients.consumer.KafkaConsumer, but tests don’t start it, so no broker required.
- src/test/kotlin/com/fsociety/ktor/kafka/consumer/manager/KtorKafkaConsumerManagerTest.kt
  - Exercises registration idempotency, start() once semantics, and stop() idempotency using relaxed MockK consumer, no real Kafka.
- src/test/kotlin/com/fsociety/ktor/kafka/plugin/KtorKafkaPluginTest.kt
  - Verifies plugin installation and ApplicationStarted/ApplicationStopping hooks via Ktor testApplication.

2.3 Running Tests
- All tests: ./gradlew test
- Single spec by FQN: ./gradlew test --tests "com.fsociety.ktor.kafka.consumer.builder.KafkaKtorConsumerBuilderTest"
- Single test via Kotest test filtering (by name) is typically done with Gradle’s pattern matching of test method names; with Kotest, it’s often easier to run the whole spec or use Kotest tags.

2.4 Adding New Tests
- Prefer Kotest DescribeSpec/FreeSpec/ShouldSpec consistently with existing tests (DescribeSpec is used here).
- For Kafka-facing code paths, avoid hitting the network:
  - Either mock KtorKafkaConsumer and verify interactions like startListening/stopListening.
  - Or ensure you don’t poll a real KafkaConsumer in tests; constructing the client is okay but don’t call poll/assign/subscribe against a live broker.
- For Ktor plugin edges, use testApplication from ktor-server-test-host; this avoids standing up a server process.
- Use MockK relaxed mocks for simple interaction verification.
- Keep thread scheduling concerns in mind; KtorKafkaConsumerManagerTest uses Thread.sleep(10) to allow coroutine scheduling before verify(). If you add more concurrency, prefer advancing virtual time or structured hooks.

2.5 Create and Run a Simple Test (Demonstration)
This is a minimal example consistent with the project stack. It was validated locally before recording here.

- Create file: src/test/kotlin/com/fsociety/ktor/kafka/SimpleSanityTest.kt
  Contents:

  package com.fsociety.ktor.kafka

  import io.kotest.core.spec.style.DescribeSpec
  import io.kotest.matchers.shouldBe

  class SimpleSanityTest : DescribeSpec({
      describe("sanity") {
          it("runs a trivial assertion to demonstrate test execution") {
              (1 + 1) shouldBe 2
          }
      }
  })

- Run: ./gradlew test
  Expected: BUILD SUCCESSFUL

- Cleanup: Remove the temporary file after verifying the run, to keep the repo clean.

2.6 Useful Gradle Flags
- --info or --debug for more verbose output when diagnosing test failures.
- --tests pattern to focus.

3. Development Notes and Pitfalls

3.1 Plugin Lifecycle and Registration
- KtorKafkaPlugin uses ApplicationStarted to register any KtorKafkaConsumerSpec entries from KtorKafkaPluginConfiguration and then starts them via KtorKafkaConsumerManager.
- On ApplicationStopping, the manager.stop() is invoked inside runBlocking and logs a final info message after requesting shutdown.
- The manager is coroutine-based and runs consumers on Dispatchers.IO under an injected CoroutineScope.

3.2 Consumer Manager Semantics
- KtorKafkaConsumerManager:
  - register() is idempotent by consumer id; duplicates are ignored with a warning.
  - start() ensures a single job per consumer id is launched; repeated start calls don’t duplicate jobs.
  - stop() is idempotent using an AtomicBoolean guard; duplicate calls are logged and ignored after the first.
  - stop() cancels jobs via cancelAndJoin and swallows/notes failures via log.warn.

3.3 Builder Validation
- KafkaKtorConsumerBuilder merges plugin-level bootstrapServers/groupId with per-consumer overrides, then enforces:
  - bootstrapServers != null
  - groupId != null
  - topics.isNotEmpty()
  - keyDeserializer and valueDeserializer present
- Any missing piece throws IllegalArgumentException with clear messages; tests assert against these.
- build() instantiates a KafkaConsumer with AUTO_OFFSET_RESET_CONFIG defaulted to earliest unless overridden via property(). It does not connect until used; safe in unit tests.

3.4 JSON (De)Serialization Helpers
- JsonSerializer/JsonDeserializer use kotlinx.serialization with JsonNamingStrategy.SnakeCase. Failures are logged and then either throw (serialize) or return null (deserialize).
- When wiring custom serializers in consumers, derive from these classes providing serializer() for your @Serializable type.

3.5 Logging
- logger() helper is used throughout (SLF4J facade, logback backend). The provided logback.xml is intentionally minimal. For local debugging, add appenders and level overrides as needed.

3.6 Code Style and Lint
- .editorconfig disables star imports by raising thresholds and sets 4-space indent.
- Run ktlintCheck in CI; run ktlintFormat locally when preparing PRs.

3.7 Version Management
- Library versions are defined in gradle.properties (koinVersion, arrowVersion, kafkaVersion, kotestVersion, etc.). Update there when bumping.

3.8 Repository Publishing
- publishing configuration creates a sources JAR and a MavenPublication from components["java"].
- A repository target is not configured; add a maven repository (e.g., GitHub Packages) under publishing.repositories when you’re ready to publish.

3.9 Configuration via Application Config
- The plugin is created with configurationPath = "ktor.kafka"; you can supply defaults via application.conf/application.yaml under that path. See README for examples. Keys differ slightly (bootstrap_servers under HOCON/yaml maps to bootstrapServers property).

4. Local Dev Recipes

- Run tests fast: ./gradlew test --no-build-cache --parallel
- Focus a single spec: ./gradlew test --tests "com.fsociety.ktor.kafka.consumer.manager.KtorKafkaConsumerManagerTest"
- Lint all: ./gradlew ktlintCheck
- Fix formatting: ./gradlew ktlintFormat
- Validate a change without network: prefer mocking KtorKafkaConsumer vs. using Kafka testcontainers. If you need integration tests later, add Testcontainers Kafka with a separate Gradle test task to avoid slowing unit tests.

5. What Was Verified While Writing This

- ./gradlew test succeeds on the current codebase.
- ktlintCheck succeeds.
- A temporary Kotest spec (SimpleSanityTest) was created, executed successfully, and then removed to keep the repository clean.

If you encounter friction not captured here, please append new findings to this document to keep the feedback loop tight.
