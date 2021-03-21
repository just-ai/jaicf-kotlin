import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Google Actions Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Google Actions Channel implementation. Enables JAICF-Kotlin integration with Google Actions SDK and Dialogflow"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api("com.google.actions:actions-on-google:1.8.0") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }

    implementation(kotlin("reflect", Version.reflect))
}