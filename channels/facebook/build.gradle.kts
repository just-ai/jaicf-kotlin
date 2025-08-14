import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Facebook Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Facebook Channel implementation. Enables JAICF-Kotlin integration with Facebook."

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.messenger4j) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)
}
