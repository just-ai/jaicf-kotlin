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
    api(libs.kotlin.telegram.bot) {
        exclude("com.github.kotlin-telegram-bot.kotlin-telegram-bot", "webhook")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okhttp3", "logging-interceptor")
        exclude("com.squareup.retrofit2", "retrofit")
        exclude("com.squareup.retrofit2", "converter-gson")
        exclude("com.squareup.okio", "okio")
    }
    api(libs.okhttp)
    api(libs.okhttp.logging.interceptor)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.mockk)
}

tasks.named<Test>("test") { useJUnitPlatform() }
