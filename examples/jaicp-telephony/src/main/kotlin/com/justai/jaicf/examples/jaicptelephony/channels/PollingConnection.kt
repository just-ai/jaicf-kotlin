package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.jaicptelephony.citiesGameBot
import com.justai.jaicf.examples.jaicptelephony.accessToken


fun main() {
    JaicpPollingConnector(
        botApi = citiesGameBot,
        accessToken = accessToken,
        channels = listOf(FacebookChannel, TelegramChannel)
    ).runBlocking()
}
