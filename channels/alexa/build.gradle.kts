import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Alexa Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Alexa Channel implementation. Enables JAICF-Kotlin integration with Amazon Alexa."

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.amazon.alexa)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
}
