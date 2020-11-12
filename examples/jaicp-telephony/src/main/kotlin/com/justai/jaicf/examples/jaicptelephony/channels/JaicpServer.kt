package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.jaicp.JaicpServer
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.jaicptelephony.accessToken
import com.justai.jaicf.examples.jaicptelephony.telephonyCallScenario

fun main() {
    JaicpServer(
        telephonyCallScenario,
        accessToken,
        channels = listOf(TelephonyChannel)
    ).start()
}