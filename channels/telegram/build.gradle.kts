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
