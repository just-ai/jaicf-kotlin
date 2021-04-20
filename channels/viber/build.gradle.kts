import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Viber Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Viber Channel implementation. Enables JAICF-Kotlin integration with Viber"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(`coroutines-core`())
    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-logging-jvm"))
    api(jackson())
}
