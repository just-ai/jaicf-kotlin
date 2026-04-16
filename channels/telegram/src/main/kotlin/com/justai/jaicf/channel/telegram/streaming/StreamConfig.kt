package com.justai.jaicf.channel.telegram.streaming

import com.pengrad.telegrambot.model.request.ParseMode

/**
 * Configuration for streaming messages in Telegram channel.
 *
 * @param updateIntervalMs Interval in milliseconds between message updates during streaming (default: 500ms)
 * @param initialPlaceholder Lambda function to generate the initial placeholder text before streaming starts (default: "...")
 * @param parseMode Optional parse mode for message formatting (Markdown, HTML, etc.)
 */
data class StreamConfig(
    val updateIntervalMs: Long = 500,
    val initialPlaceholder: () -> String = { "..." },
    val parseMode: ParseMode = ParseMode.Markdown,
)
