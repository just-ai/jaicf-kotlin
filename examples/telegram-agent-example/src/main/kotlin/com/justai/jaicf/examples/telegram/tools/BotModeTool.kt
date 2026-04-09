package com.justai.jaicf.examples.telegram.tools

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.justai.jaicf.activator.llm.tool.llmTool

/**
 * Bot mode query definition.
 * Returns information about the current operating mode (polling or webhook).
 */
@JsonClassDescription("Check the bot's current operating mode (polling or webhook)")
data class BotModeQuery(
    @field:JsonPropertyDescription("Level of detail: 'basic' or 'detailed'")
    val detail: String = "basic"
)

// Bot mode tool
val BotModeTool = llmTool<BotModeQuery> {
    val webhookUrl = System.getenv("TELEGRAM_WEBHOOK_URL")
    val mode = if (webhookUrl != null) "webhook" else "polling"

    buildString {
        appendLine("🤖 Bot Operating Mode: ${mode.uppercase()}")
        appendLine()
        when (mode) {
            "webhook" -> {
                appendLine("📡 Webhook Mode")
                appendLine("URL: $webhookUrl")
                appendLine()
                appendLine("In webhook mode, Telegram sends updates to this bot via HTTP POST requests.")
                appendLine("This is the recommended mode for production deployments.")
            }
            else -> {
                appendLine("🔄 Polling Mode")
                appendLine()
                appendLine("In polling mode, the bot actively requests updates from Telegram servers.")
                appendLine("This is suitable for local development and testing.")
            }
        }
    }
}
