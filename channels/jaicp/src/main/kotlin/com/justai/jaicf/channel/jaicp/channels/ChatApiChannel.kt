package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.dto.ChatApiBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.reactions.ChatApiReactions

class ChatApiChannel(override val botApi: BotApi) : JaicpNativeChannel(botApi) {

    override fun createRequest(request: JaicpBotRequest) = ChatApiBotRequest(request)
    override fun createReactions() = ChatApiReactions()

    companion object : JaicpNativeChannelFactory {
        override val channelType = "chatapi"
        override fun create(botApi: BotApi) = ChatApiChannel(botApi)
    }
}