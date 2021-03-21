import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Dialogflow Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Dialogflow Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api("com.google.cloud:google-cloud-dialogflow:0.109.0-alpha")
    api("io.grpc:grpc-okhttp:1.24.0")
}
