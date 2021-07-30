package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.examples.multilingual.MultilingualBotEngine
import com.justai.jaicf.examples.multilingual.mainAccessToken


fun main() {
    JaicpPollingConnector(
        botApi = MultilingualBotEngine,
        accessToken = mainAccessToken,
        channels = listOf(ChatWidgetChannel)
    ).runBlocking()
}
