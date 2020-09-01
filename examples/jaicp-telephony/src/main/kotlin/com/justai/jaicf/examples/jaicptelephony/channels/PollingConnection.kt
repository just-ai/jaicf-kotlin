package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.facebook.FacebookChannel
import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.channels.ChatApiChannel
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.jaicptelephony.*
import io.ktor.client.features.logging.LogLevel


fun main() {
    JaicpPollingConnector(
        botApi = telephonyCallScenario,
        accessToken = accessToken,
        channels = listOf(TelegramChannel, FacebookChannel, ChatWidgetChannel),
        url = caUrl,
    logLevel = LogLevel.INFO
    ).runBlocking()
}
