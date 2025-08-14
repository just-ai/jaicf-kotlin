import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Yandex Alice Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Yandex Alice Channel implementation. Enables JAICF-Kotlin integration with Alice."

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.serialization)
    api(libs.ktor.kotlinx.serialization)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.logging)
    implementation(libs.kotlin.reflect)

    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)
}
