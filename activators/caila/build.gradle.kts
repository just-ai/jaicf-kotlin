import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Caila NLU Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Caila NLU Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.ktor.client.cio)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.serialization)
    api(libs.ktor.kotlinx.serialization)
    api(libs.ktor.client.content.negotiation)
    implementation(platform(libs.ktor.bom))

    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.mockk)
    testImplementation(libs.ktor.mockk)
    testImplementation(libs.bundles.junit)
}

tasks.named<Test>("test") { useJUnitPlatform() }
