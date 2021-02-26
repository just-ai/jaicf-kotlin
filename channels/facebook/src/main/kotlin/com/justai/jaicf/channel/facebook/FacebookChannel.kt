package com.justai.jaicf.channel.facebook

import com.github.messenger4j.exception.MessengerVerificationException
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.facebook.api.FacebookInvocationRequest
import com.justai.jaicf.channel.facebook.api.toBotRequest
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asTextHttpBotResponse
import com.justai.jaicf.channel.invocationapi.*
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.context.RequestContext
import java.util.*

class FacebookChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleAsyncBotChannel,
    InvocableBotChannel {

    private lateinit var messenger: Messenger

    constructor(botApi: BotApi, config: FacebookPageConfig) : this(botApi) {
        messenger = Messenger.create(config.pageAccessToken, config.appSecret, config.verifyToken)
    }

    private constructor(botApi: BotApi, baseUrl: String) : this(botApi) {
        messenger = Messenger.create("", "", "", baseUrl)
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
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

        internal const val REQUEST_TEMPLATE_PATH = "/FacebookRequestTemplate.json"
    }

    private fun generateRequestFromTemplate(request: InvocationRequest) =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", System.currentTimeMillis().toString())
            .replace("{{ messageId }}", UUID.randomUUID().toString())


    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generatedRequest = generateRequestFromTemplate(request)
        messenger.onReceiveEvents(generatedRequest, Optional.empty()) { event ->
            FacebookInvocationRequest.create(request, event.asTextMessageEvent())?.let {
                botApi.process(it, FacebookReactions(messenger, it), requestContext)
            }
        }
    }
}
