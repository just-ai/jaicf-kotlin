import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Slack Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Aimybox Slack implementation. Enables JAICF-Kotlin integration with Slack"

plugins {
    alias(libs.plugins.kotlin.jvm)
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.kotlinx.coroutines.core)
    api(libs.slack)

    implementation(libs.kotlin.stdlib)
}