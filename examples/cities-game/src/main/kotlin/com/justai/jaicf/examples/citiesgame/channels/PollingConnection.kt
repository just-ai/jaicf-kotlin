package com.justai.jaicf.examples.citiesgame.channels

import com.justai.jaicf.examples.citiesgame.citiesGameBot
import com.justai.jaicf.examples.citiesgame.projectId
import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel


fun main() {
    JaicpPollingConnector(
        botApi = citiesGameBot,
        projectId = projectId,
        channels = listOf(TelephonyChannel, ChatWidgetChannel)
    ).runBlocking()
}
