package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.jaicp.JaicpServer
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.multilingual.RoutingEngine
import com.justai.jaicf.examples.multilingual.bots.MainBot

fun main() {
    JaicpServer(
        botApi = RoutingEngine,
        accessToken = MainBot.accessToken,
        channels = listOf(TelephonyChannel)
    ).start()
}
