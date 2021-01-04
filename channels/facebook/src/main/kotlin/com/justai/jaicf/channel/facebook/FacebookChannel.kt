package com.justai.jaicf.channel.facebook

import com.github.messenger4j.exception.MessengerVerificationException
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.channel.facebook.api.toBotRequest
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asTextHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.reactions.Reactions
import java.util.*

class FacebookChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleAsyncBotChannel {

    private lateinit var messenger: Messenger

    constructor(botApi: BotApi, config: FacebookPageConfig) : this(botApi) {
        messenger = Messenger.create(config.pageAccessToken, config.appSecret, config.verifyToken)
    }

    private constructor(botApi: BotApi, baseUrl: String) : this(botApi) {
        messenger = Messenger.create("", "", "", baseUrl)
    }

    override fun processLiveChatEventRequest(event: String, reactions: Reactions) {
        (reactions as? FacebookReactions)?.let {
            botApi.process(EventBotRequest(it.request.clientId, event), it, RequestContext.DEFAULT)
        }
    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        messenger.onReceiveEvents(request.receiveText(), Optional.empty()) { event ->
            event.toBotRequest().let { botRequest ->
                botApi.process(
                    botRequest,
                    FacebookReactions(messenger, botRequest),
                    RequestContext.fromHttp(request)
                )
            }
        }
        return "".asTextHttpBotResponse()
    }

    fun verifyToken(mode: String?, token: String?, challenge: String?): String {
        return try {
            messenger.verifyWebhook(mode.orEmpty(), token.orEmpty())
            challenge.orEmpty()
        } catch (e: MessengerVerificationException) {
            ""
        }
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "facebook"
        override fun create(botApi: BotApi, apiUrl: String) = FacebookChannel(botApi, apiUrl)
    }
}