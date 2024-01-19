package com.justai.jaicf.examples.llm.channel

import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.llm.llmBot

fun main() {
    TelegramChannel(llmBot, System.getenv("TELEGRAM_BOT_TOKEN")).run()
}