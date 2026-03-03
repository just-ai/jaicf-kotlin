package com.justai.jaicf.channel.telegram.streaming

import com.pengrad.telegrambot.model.request.ParseMode

/**
 * Configuration for streaming messages in Telegram channel.
 *
 * @param updateIntervalMs Interval in milliseconds between message updates during streaming (default: 500ms)
 * @param initialPlaceholder Text to show in the initial message before streaming starts (default: "...")
 * @param parseMode Optional parse mode for message formatting (Markdown, HTML, etc.)
 */
data class StreamConfig(
    val updateIntervalMs: Long = 500,
    val initialPlaceholder: String = "...",
    val parseMode: ParseMode? = null
)
