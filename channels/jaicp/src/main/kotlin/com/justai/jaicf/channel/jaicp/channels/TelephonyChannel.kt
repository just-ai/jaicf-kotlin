package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.TelephonyBotRequest
import com.justai.jaicf.channel.jaicp.reactions.TelephonyReactions

class TelephonyChannel(override val botApi: BotApi) : JaicpNativeChannel(botApi) {

    override fun createRequest(request: JaicpBotRequest) = TelephonyBotRequest(request)
    override fun createReactions() = TelephonyReactions()

    companion object : JaicpNativeChannelFactory {
        override val channelType = "resterisk"
        override fun create(botApi: BotApi) = TelephonyChannel(botApi)
    }
}