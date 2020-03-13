package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotChannel
import com.justai.jaicf.channel.jaicp.JaicpBotChannel
import com.justai.jaicf.channel.jaicp.JaicpChannelFactory
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse


interface JaicpNativeBotChannel : JaicpBotChannel, HttpBotChannel {
    fun process(request: JaicpBotRequest): JaicpBotResponse
}

interface JaicpNativeChannelFactory : JaicpChannelFactory {
    fun create(botApi: BotApi): JaicpNativeBotChannel
    override val channelType: String
}