import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Telegram Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Telegram Channel implementation. Enables JAICF-Kotlin integration with Telegram"

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.java.telegram.bot.api)
    api(libs.okhttp)
    api(libs.okhttp.logging.interceptor)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization)
}
