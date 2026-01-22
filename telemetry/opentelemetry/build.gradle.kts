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
    implementation(libs.kotlin.stdlib)
}