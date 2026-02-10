import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin OpenTelemetry integration"
ext[POM_DESCRIPTION] = "Optional OpenTelemetry tracing support for JAICF BotEngine."

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.sdk.trace)
    implementation("io.opentelemetry:opentelemetry-exporter-logging:1.43.0")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.43.0")
    implementation(libs.kotlin.stdlib)
}