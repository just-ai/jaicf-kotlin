package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.examples.multilingual.RoutingEngine
import com.justai.jaicf.examples.multilingual.bots.MainBot


fun main() {
    JaicpPollingConnector(
        botApi = RoutingEngine,
        accessToken = MainBot.accessToken,
        channels = listOf(ChatWidgetChannel)
    ).runBlocking()
}
