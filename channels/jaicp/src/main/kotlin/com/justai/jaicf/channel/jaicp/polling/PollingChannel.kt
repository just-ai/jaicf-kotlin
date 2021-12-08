package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.JaicpBotChannel

internal data class PollingChannel(
    val url: String,
    val botChannel: JaicpBotChannel,
    val poller: RequestPoller,
) {
    var isActive: Boolean = false
        private set

    fun startPolling() {
        isActive = true
        poller.startPolling()
    }

    fun stopPolling() {
        isActive = false
        poller.stopPolling()
    }
}