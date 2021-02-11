package com.justai.jaicf.channel.facebook

import com.github.messenger4j.exception.MessengerVerificationException
import com.github.messenger4j.webhook.Event
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.facebook.api.FacebookLiveChatEventRequest
import com.justai.jaicf.channel.facebook.api.toBotRequest
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asTextHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
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

    override fun processLiveChatEventRequest(event: String, clientId: String, request: HttpBotRequest) {
        messenger.onReceiveEvents(request.receiveText(), Optional.empty()) { e: Event ->
            val eventRequest = FacebookLiveChatEventRequest(e.toBotRequest().event, clientId, event)
            val reactions = FacebookReactions(messenger, eventRequest)
            botApi.process(eventRequest, reactions, RequestContext.fromHttp(request))
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