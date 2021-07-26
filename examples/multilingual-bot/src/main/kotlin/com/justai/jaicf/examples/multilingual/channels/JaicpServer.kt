package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.jaicp.JaicpServer
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.multilingual.MainBot
import com.justai.jaicf.examples.multilingual.MultilingualBotEngine

fun main() {
    JaicpServer(
        botApi = MultilingualBotEngine,
        accessToken = MainBot.accessToken,
        channels = listOf(TelephonyChannel)
    ).start()
}
