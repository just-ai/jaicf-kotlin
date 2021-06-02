import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Facebook Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Facebook Channel implementation. Enables JAICF-Kotlin integration with Facebook."

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    core()
    api("com.github.messenger4j:messenger4j:1.1.0") {
        exclude("org.jetbrains.kotlin")
    }
}
