import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Max Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Max Channel implementation. Enables JAICF-Kotlin integration with Max messenger"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
}
