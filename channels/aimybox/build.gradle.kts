import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Aimybox Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Aimybox Channel implementation. Enables JAICF-Kotlin integration with Aimybox"

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    core()
}
