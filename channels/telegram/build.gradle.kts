plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.0.1") {
        exclude("com.github.kotlin-telegram-bot.kotlin-telegram-bot", "webhook")
    }
}
