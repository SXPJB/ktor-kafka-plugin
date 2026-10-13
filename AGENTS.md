# AGENTS.md

Context for AI coding agents. Keep it short, accurate, and updated when architecture changes.

## Project
Ktor server plugin that manages Apache Kafka consumers (producers planned, not implemented).
Library, not an app: no `main`, published via `maven-publish` (no remote repo configured yet).

- Language: Kotlin 2.1.10, JVM 17 (CI uses Corretto 17)
- Ktor 3.2.x, kafka-clients 4.0.0, kotlinx.serialization, kotlinx.coroutines, SLF4J/Logback
- Tests: Kotest 5.9 (`DescribeSpec`), MockK, `ktor-server-test-host`
- Declared but unused: Koin, Arrow, kafka-streams. Do not add usages without asking; prefer removing if touched.
- Versions live in `gradle.properties` (plugin versions in `build.gradle.kts`).

## Commands
```bash
./gradlew ktlintFormat   # auto-fix style
./gradlew ktlintCheck    # lint (CI gate 1)
./gradlew test           # tests (CI gate 2)
./gradlew build          # full build
```
Always run `ktlintCheck` and `test` before declaring work done. CI (`.github/workflows/build-test.yml`) runs both on PRs to `master`.

## Layout (`src/main/kotlin/com/fsociety/ktor/kafka`)
| Package | Contents |
|---|---|
| `plugin` | `KtorKafkaPlugin`: `createApplicationPlugin`, config path `ktor.kafka`, lifecycle hooks |
| `config` | `KtorKafkaConfig` (sealed base: `bootstrapServers`, `groupId`), `KtorKafkaPluginConfiguration` (plugin DSL root, holds registrations), `KtorKafkaConsumerConfig<K,V>` (per-consumer data class) |
| `consumer` | `KtorKafkaConsumer<K,V>`: poll loop on a coroutine, `startListening`/`stopListening` |
| `consumer.builder` | `KafkaKtorConsumerBuilder<K,V>`: DSL (`configure`, `listener`, `property`), validation, creates `KafkaConsumer` |
| `consumer.manager` | `KtorKafkaConsumerManager`: registers by id, starts once, idempotent `stop()` |
| `consumer.model` | `KtorKafkaConsumerWrapper`: consumer + listener pair |
| `shared` | `KtorKafkaSpec` (sealed interface, `id`), `KtorKafkaConsumerSpec<K,V>` |
| `serialization.json` | `JsonSerializer<T>` / `JsonDeserializer<T>` (kotlinx, snake_case naming) |
| `utils` | `logger()` reified SLF4J extension |

## Runtime flow
1. `install(KtorKafkaPlugin) { ... }` builds `KtorKafkaPluginConfiguration` from `ApplicationConfig` at `ktor.kafka` (keys: `bootstrap_servers`, `groupId`).
2. Each `consumer<K, V> { configure { ... }; listener { k, v -> } }` runs the builder; `build()` validates and stores a `KtorKafkaConsumerSpec`.
3. On `ApplicationStarted`: specs are dispatched with an exhaustive `when` over `KtorKafkaSpec`, registered in the manager, then `start()`.
4. On `ApplicationStopping`: `runBlocking { manager.stop() }`.

Validation errors are `IllegalArgumentException` via `require`/`requireNotNull`, messages are constants in the builder companion (tests assert on them).

## Conventions
- Style: ktlint `intellij_idea`, 4 spaces, no wildcard imports, LF, final newline (see `.editorconfig`).
- Log/error messages: `private const val` in a `companion object` (or file-level), SLF4J `{}` placeholders, never string interpolation in logs.
- Logger: `private val log = logger()`.
- Public API classes carry KDoc. Keep it when editing; add it to new public types.
- Thread-safety: `AtomicBoolean` for state flags, `ConcurrentHashMap` for registries.
- Immutable updates on config (`copy()`), expose read-only views (`registrations` returns a copy).
- New spec types (e.g. producers): add an implementation of `KtorKafkaSpec`, then handle it in the `when` in `KtorKafkaPlugin` (sealed => compiler enforces it).
- Mirror the main package structure in `src/test`. One `DescribeSpec` per class, `describe`/`it` with behavior sentences. Mock `Consumer` with MockK; do not require a real broker in unit tests.

## Known issues / gotchas (verify before relying on them)
- Precedence: in `KafkaKtorConsumerBuilder.requireValidConfig`, plugin-level `bootstrapServers`/`groupId` override per-consumer values. Likely inverted vs. intent.
- `KtorKafkaConsumer`: `poll()` is blocking, so `Job.cancel()` does not interrupt it; shutdown relies on the `isRunning` flag. No `wakeup()` call. `KafkaConsumer` is not thread-safe.
- Listener is a non-suspending `(K, V) -> Unit`; exceptions in it stop the consumer loop (caught, logged, consumer closed).
- Auto-commit uses Kafka defaults; `auto.offset.reset` defaults to `earliest` unless overridden via `property(...)`.
- `src/main/resources/application.conf` is empty; `logback.xml` ships inside the library jar.
- Maven `group` is `com.fsociety.ktor.pluging` and `rootProject.name` is `ktor-kafka-pluging` (typo "pluging"), while README uses `com.fsociety.ktor:ktor-kafka-plugin`. Do not "fix" silently: it changes published coordinates.

## Git
- Default branch `master`; never commit on it. Work on a branch and PR into `master`.
- Branches: `feature/`, `task/`, or `fix/` + kebab-case description.
- Commit/PR title format: `<Type> | SXPJB | <Summary>` (`Feature`, `Task`, `Fix`, `Chore`).
- Full workflow for starting any development: `.claude/skills/start-development/SKILL.md`.

## Agent rules
- Write code, comments, docs, and commits in English.
- Keep changes scoped; do not bump dependency versions or change public API/coordinates unless asked.
- Update this file and `README.md` when you change architecture, public DSL, or commands.
