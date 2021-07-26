package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.examples.multilingual.MainBot
import com.justai.jaicf.examples.multilingual.MultilingualBotEngine


fun main() {
    JaicpPollingConnector(
        botApi = MultilingualBotEngine,
        accessToken = MainBot.accessToken,
        channels = listOf(ChatWidgetChannel)
    ).runBlocking()
}
