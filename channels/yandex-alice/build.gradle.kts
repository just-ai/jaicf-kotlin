import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Yandex Alice Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Yandex Alice Channel implementation. Enables JAICF-Kotlin integration with Alice."

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-serialization-jvm"))
    api(ktor("ktor-client-logging-jvm"))
    implementation(kotlin("reflect", Version.reflect))
}
