package com.justai.jaicf.examples.citiesgame.channels

import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.citiesgame.citiesGameBot
import com.justai.jaicf.examples.citiesgame.projectId


fun main() {
    JaicpPollingConnector(
        botApi = citiesGameBot,
        projectId = projectId,
        channels = listOf(FacebookChannel, TelegramChannel)
    ).runBlocking()
}
