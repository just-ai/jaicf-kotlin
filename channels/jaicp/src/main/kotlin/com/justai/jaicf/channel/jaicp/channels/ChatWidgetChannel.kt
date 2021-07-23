package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.jaicp.dto.ChatWidgetBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.reactions.ChatWidgetReactions

/**
 * JAICP ChatWidget channel
 *
 * This channel can be used for processing messages from Web Widget, which can be configured JAICP Web Interface
 * and further embedded into your website.
 * Client information can be retrieved from [ChatWidgetBotRequest].
 *
 * @see ChatWidgetReactions
 * @see ChatWidgetBotRequest
 * @see JaicpNativeChannel
 *
 * @param botApi the [BotApi] implementation used to process the requests to this channel
 * */
class ChatWidgetChannel(
    override val botApi: BotApi,
    private val liveChatProvider: JaicpLiveChatProvider,
    mutedEvents: List<String> = emptyList()
) : JaicpNativeChannel(botApi, mutedEvents) {

    override fun createRequest(request: JaicpBotRequest) = ChatWidgetBotRequest.create(request)
    override fun createReactions() = ChatWidgetReactions(liveChatProvider)

    companion object : JaicpNativeChannelFactory {
        override val channelType = "chatwidget"
        override fun create(botApi: BotApi, liveChatProvider: JaicpLiveChatProvider) =
            ChatWidgetChannel(botApi, liveChatProvider)
    }

    class Factory(
        private val mutedEvents: List<String> = emptyList()
    ) : JaicpNativeChannelFactory {
        override val channelType = "chatwidget"
        override fun create(botApi: BotApi, liveChatProvider: JaicpLiveChatProvider): JaicpNativeBotChannel =
            ChatWidgetChannel(botApi, liveChatProvider, mutedEvents)
    }
}
