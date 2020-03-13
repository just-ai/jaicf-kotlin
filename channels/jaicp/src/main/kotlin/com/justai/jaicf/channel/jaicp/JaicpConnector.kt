package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.JaicpChannelFactory

interface JaicpConnector {
    val botApi: BotApi
    val channels: List<JaicpChannelFactory>
}