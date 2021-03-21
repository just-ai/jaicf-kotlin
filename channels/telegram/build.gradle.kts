import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Telegram Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Telegram Channel implementation. Enables JAICF-Kotlin integration with Telegram"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.0.1") {
        exclude("com.github.kotlin-telegram-bot.kotlin-telegram-bot", "webhook")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
}
