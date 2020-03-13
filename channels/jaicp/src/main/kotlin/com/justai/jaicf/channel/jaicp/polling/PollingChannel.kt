package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.JaicpBotChannel

data class PollingChannel(
    val url: String,
    val botChannel: JaicpBotChannel
)