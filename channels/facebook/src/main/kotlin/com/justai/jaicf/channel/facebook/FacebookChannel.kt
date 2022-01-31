package com.justai.jaicf.channel.facebook

import com.github.messenger4j.exception.MessengerVerificationException
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.facebook.api.FacebookInvocationRequest
import com.justai.jaicf.channel.facebook.api.toBotRequest
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.http.withTrailingSlash
import java.util.*

class FacebookChannel private constructor(
    override val botApi: BotApi
) : JaicpCompatibleAsyncBotChannel,
    InvocableBotChannel {

    private lateinit var messenger: Messenger
    private var liveChatProvider: JaicpLiveChatProvider? = null

    constructor(botApi: BotApi, config: FacebookPageConfig) : this(botApi) {
        messenger = Messenger.create(config.pageAccessToken, config.appSecret, config.verifyToken)
    }

    private constructor(botApi: BotApi, baseUrl: String, liveChatProvider: JaicpLiveChatProvider) : this(botApi) {
        messenger = Messenger.create("", "", "", baseUrl.withTrailingSlash(false))
        this.liveChatProvider = liveChatProvider
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        messenger.onReceiveEvents(request.receiveText(), Optional.empty()) { event ->
            event.toBotRequest().let { botRequest ->
                botApi.process(
                    botRequest,
                    FacebookReactions(messenger, botRequest, liveChatProvider),
                    RequestContext.fromHttp(request)
                )
            }
        }
        return HttpBotResponse.accepted()
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
        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ): JaicpCompatibleAsyncBotChannel = FacebookChannel(botApi, apiUrl, liveChatProvider)

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
                botApi.process(it, FacebookReactions(messenger, it, liveChatProvider), requestContext)
            }
        }
    }
}
