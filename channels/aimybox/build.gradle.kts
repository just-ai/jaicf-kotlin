import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Aimybox Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Aimybox Channel implementation. Enables JAICF-Kotlin integration with Aimybox"

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    `jaicf-publish`
}

dependencies {
    core()
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)
}
