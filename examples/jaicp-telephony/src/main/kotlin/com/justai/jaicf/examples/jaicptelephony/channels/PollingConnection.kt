package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.examples.jaicptelephony.accessToken
import com.justai.jaicf.examples.jaicptelephony.citiesGameBot


fun main() {
    JaicpPollingConnector(
        botApi = citiesGameBot,
        accessToken = accessToken,
        channels = listOf(TelephonyChannel)
    ).runBlocking()
}
