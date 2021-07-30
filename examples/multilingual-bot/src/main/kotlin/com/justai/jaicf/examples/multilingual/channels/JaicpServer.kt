package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.jaicp.JaicpServer
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.multilingual.MultilingualBotEngine
import com.justai.jaicf.examples.multilingual.mainAccessToken

fun main() {
    JaicpServer(
        botApi = MultilingualBotEngine,
        accessToken = mainAccessToken,
        channels = listOf(TelephonyChannel)
    ).start()
}
