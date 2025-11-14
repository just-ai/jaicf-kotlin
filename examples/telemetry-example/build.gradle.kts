plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization)

    core()
    implementation(project(":telemetry:opentelemetry"))
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.sdk.trace)
    implementation("io.opentelemetry:opentelemetry-exporter-logging:1.43.0")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.43.0")

    implementation(libs.slf4j.simple)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.43.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

