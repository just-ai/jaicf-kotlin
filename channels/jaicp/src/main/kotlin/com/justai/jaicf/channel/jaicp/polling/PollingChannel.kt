package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.JaicpBotChannel

internal data class PollingChannel(
    val url: String,
    val botChannel: JaicpBotChannel,
    val poller: RequestPoller
)