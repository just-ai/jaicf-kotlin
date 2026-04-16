plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.justai.jaicf.caila-publish-plugin")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    // JAICF core
    core()

    // Activators
    implementation(project(":activators:llm"))

    // Channels
    implementation(project(":channels:telegram"))

    // Ktor server for health check and webhook
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // Logging
    implementation(libs.slf4j.simple)
}

application {
    mainClass.set("com.justai.jaicf.examples.telegram.TelegramAgentMainKt")
}

cailaPublish {
    docker {
        javaApplication {
            mainClassName = "com.justai.jaicf.examples.telegram.TelegramAgentMainKt"
            images = listOf("${providers.gradleProperty("dockerUsername").getOrElse("user")}/telegram-agent-bot:latest")
            ports = listOf(8080)
            jvmArgs = listOf("-Xms256m", "-Xmx512m")
        }

        registryCredentials {
            username.set(providers.gradleProperty("dockerUsername").getOrElse(""))
            password.set(providers.gradleProperty("dockerPassword").getOrElse(""))
        }
    }

    image {
        name = "telegram-agent-bot-image"
        allowDestructiveUpdate = true
    }

    model {
        name = "telegram-agent-bot"
        displayName = "Telegram Agent Bot Example"
        displayAuthor = "JAICF Team"
        shortDescription = "Telegram bot with LLM agent and streaming support"

        http {
            port = 8080
            mainPageEndpoint = "/health"
        }

        publicSettings {
            isPublic = true
        }

        s3 {
            enabled = true
            prefix = "telegram-bot-contexts"
            region = "ru"
        }

        environmentVariables {
            put("TELEGRAM_BOT_TOKEN", "\${TELEGRAM_BOT_TOKEN}")
            put("TELEGRAM_WEBHOOK_URL", "\${TELEGRAM_WEBHOOK_URL}")
            put("OPENAI_API_KEY", "\${OPENAI_API_KEY}")
            put("OPENAI_BASE_URL", "\${OPENAI_BASE_URL}")
        }
    }
}
