import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin RASA Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin RASA Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.ktor.client.cio)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.serialization)
    api(libs.ktor.kotlinx.serialization)
    api(libs.ktor.client.content.negotiation)

    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.ktor.mockk)
    testImplementation(libs.bundles.junit)
}

tasks.named<Test>("test") { useJUnitPlatform() }
