package com.justai.jaicf.examples.telegram

import com.justai.jaicf.channel.http.httpBotRouting
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.channel.telegram.streaming.StreamConfig
import com.pengrad.telegrambot.model.request.ParseMode
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Main entry point for Telegram agent bot.
 *
 * Supports two modes:
 * - Polling mode (local development): When TELEGRAM_WEBHOOK_URL is not set
 * - Webhook mode (production/CAILA): When TELEGRAM_WEBHOOK_URL is set
 *
 * Required environment variables:
 * - TELEGRAM_BOT_TOKEN: Bot token from @BotFather
 * - OPENAI_API_KEY: OpenAI API key
 * - OPENAI_BASE_URL: OpenAI API base URL (optional, defaults to https://api.openai.com/v1)
 * - TELEGRAM_WEBHOOK_URL: Webhook URL (optional, for webhook mode)
 * - PORT: Server port (optional, defaults to 8080)
 */
suspend fun main() {
    val bot = TelegramAgentBot.asBot

    val streamConfig = StreamConfig(
        updateIntervalMs = 300,
        initialPlaceholder = { "⏳ Thinking..." },
        parseMode = ParseMode.Markdown
    )

    val telegramChannel = TelegramChannel(
        botApi = bot,
        telegramBotToken = System.getenv("TELEGRAM_BOT_TOKEN")
            ?: error("TELEGRAM_BOT_TOKEN environment variable is required"),
        streamConfig = streamConfig
    )

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    val server = embeddedServer(Netty, port = port) {
        routing {
            // Health check endpoint for CAILA
            get("/health") {
                call.respondText("OK", ContentType.Text.Plain)
            }

            // Telegram webhook endpoint
            httpBotRouting("/telegram" to telegramChannel)
        }
    }

    val webhookUrl = System.getenv("TELEGRAM_WEBHOOK_URL")

    if (webhookUrl != null) {
        println("🚀 Starting in WEBHOOK mode")
        println("📡 Webhook URL: $webhookUrl")
        println("🏥 Health check: http://localhost:$port/health")

        telegramChannel.startWebhook(
            embeddedServer = server,
            webhookUrl = webhookUrl
        )
    } else {
        println("🚀 Starting in POLLING mode")
        println("🏥 Health check: http://localhost:$port/health")
        println("💬 Bot is ready to receive messages...")

        server.start(wait = false)

        withContext(Dispatchers.IO) {
            telegramChannel.startLongPolling()
        }
    }
}
