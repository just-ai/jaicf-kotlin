import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin RASA Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin RASA Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    core()
    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-serialization-jvm"))
    api(ktor("ktor-client-logging-jvm"))
}
