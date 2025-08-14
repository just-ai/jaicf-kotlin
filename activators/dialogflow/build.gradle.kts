import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Dialogflow Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Dialogflow Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.google.dialogflow)
    api(libs.grpc.okhttp)

    implementation(libs.kotlin.stdlib)
}
