val koinVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val arrowVersion: String by project
val kafkaVersion: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.2.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
    id("maven-publish")
    id("java-library")
}

group = "com.fsociety.ktor"
version = "0.0.1-ALPHA"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // Koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
    // Kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    implementation("org.apache.kafka:kafka-streams:$kafkaVersion")

    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks.register<Jar>("sourceJar") {
    group = "build"
    description = "Assembles a jar archive containing the main sources."
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])

            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = project.name
        }
    }

    repositories {
        // TODO: Add some repository like github.package
    }
}
