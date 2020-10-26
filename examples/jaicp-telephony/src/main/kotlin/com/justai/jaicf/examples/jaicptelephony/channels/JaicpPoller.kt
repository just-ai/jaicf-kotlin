package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.jaicptelephony.accessToken
import com.justai.jaicf.examples.jaicptelephony.telephonyCallScenario


fun main() {
    JaicpPollingConnector(
        botApi = telephonyCallScenario,
        accessToken = accessToken,
        channels = listOf(TelephonyChannel)
    ).runBlocking()
}
