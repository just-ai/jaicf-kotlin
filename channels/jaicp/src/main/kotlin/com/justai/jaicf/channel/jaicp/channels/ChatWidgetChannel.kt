package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.dto.ChatWidgetBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.reactions.ChatWidgetReactions

class ChatWidgetChannel(override val botApi: BotApi) : JaicpNativeChannel(botApi) {

    override fun createRequest(request: JaicpBotRequest) = ChatWidgetBotRequest(request)
    override fun createReactions() = ChatWidgetReactions()

    companion object : JaicpNativeChannelFactory {
        override val channelType = "chatwidget"
        override fun create(botApi: BotApi) = ChatWidgetChannel(botApi)
    }
}