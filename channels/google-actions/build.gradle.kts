import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Google Actions Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Google Actions Channel implementation. Enables JAICF-Kotlin integration with Google Actions SDK and Dialogflow"

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.actions.on.google) {
        exclude("org.jetbrains.kotlin")
    }

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
}
