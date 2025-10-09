import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Viber Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Viber Channel implementation. Enables JAICF-Kotlin integration with Viber"

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.logging)
    api(libs.jackson.module.kotlin)

    implementation(libs.kotlin.stdlib)
}
