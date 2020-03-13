package com.justai.jaicf.channel.facebook

import com.github.messenger4j.exception.MessengerVerificationException
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.facebook.api.*
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import java.util.*

class FacebookChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleBotChannel {

    private lateinit var messenger: Messenger

    constructor(botApi: BotApi, config: FacebookPageConfig) : this(botApi) {
        messenger = Messenger.create(config.pageAccessToken, config.appSecret, config.verifyToken)
    }

    private constructor(botApi: BotApi, baseUrl: String): this(botApi) {
        messenger = Messenger.create("%s", "", "", baseUrl)
    }

    override fun process(input: String): String? {
        messenger.onReceiveEvents(input, Optional.empty()) { event ->
            event.toBotRequest().let { request ->
                botApi.process(request, FacebookReactions(messenger, request))
            }
        }
        return ""
    }

    fun verifyToken(mode: String?, token: String?, challenge: String?): String {
        return try {
            messenger.verifyWebhook(mode.orEmpty(), token.orEmpty())
            challenge.orEmpty()
        } catch (e: MessengerVerificationException) {
            ""
        }
    }

    companion object: JaicpCompatibleAsyncChannelFactory {
        override val channelType = "facebook"
        override fun create(botApi: BotApi, apiUrl: String) = FacebookChannel(botApi, apiUrl)
    }
}