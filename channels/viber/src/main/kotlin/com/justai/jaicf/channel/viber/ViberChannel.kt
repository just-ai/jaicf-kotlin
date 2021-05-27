package com.justai.jaicf.channel.viber

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.viber.api.ViberInvocationRequest
import com.justai.jaicf.channel.viber.api.toBotRequest
import com.justai.jaicf.channel.viber.sdk.Request
import com.justai.jaicf.channel.viber.sdk.api.ViberApi
import com.justai.jaicf.channel.viber.sdk.api.ViberHttpClient
import com.justai.jaicf.channel.viber.sdk.api.ViberKtorClient
import com.justai.jaicf.channel.viber.sdk.asViberRequest
import com.justai.jaicf.channel.viber.sdk.event.IncomingConversationStartedEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingDeliveredEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingFailedEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingMessageEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingSeenEvent
import com.justai.jaicf.channel.viber.sdk.event.IncomingWebhookEvent
import com.justai.jaicf.channel.viber.sdk.event.sender
import com.justai.jaicf.channel.viber.sdk.event.senderId
import com.justai.jaicf.channel.viber.sdk.profile.BotProfile
import com.justai.jaicf.channel.viber.sdk.profile.UserProfile
import com.justai.jaicf.context.RequestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.*

class ViberChannel private constructor(
    override val botApi: BotApi,
    private val viberApi: ViberApi,
    private val botProfile: BotProfile,
    private val authToken: String,
    private val channelConfig: ViberChannelConfig,
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel {

    private val threadPool = Executors.newWorkStealingPool().asCoroutineDispatcher()
    private var liveChatProvider: JaicpLiveChatProvider? = null

    constructor(
        botApi: BotApi,
        botConfig: ViberBotConfig,
        client: ViberHttpClient = ViberKtorClient(),
        channelConfig: ViberChannelConfig = ViberChannelConfig(),
    ) : this(botApi, ViberApi(client), BotProfile(botConfig.botName), botConfig.authToken, channelConfig)

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val viberRequest = request.receiveText().asViberRequest()

        when (viberRequest.event) {
            is IncomingConversationStartedEvent -> processRequest(viberRequest, RequestContext(true, request))
            is IncomingFailedEvent -> logger.warn(viberRequest.event.description)
            is IncomingWebhookEvent -> return HttpBotResponse.ok()

            is IncomingSeenEvent -> {
                if (!channelConfig.ignoreSeenEvents)
                    processRequest(viberRequest, RequestContext.fromHttp(request))
            }

            is IncomingDeliveredEvent -> {
                if (!channelConfig.ignoreDeliveredEvents)
                    processRequest(viberRequest, RequestContext.fromHttp(request))
            }

            else -> processRequest(viberRequest, RequestContext.fromHttp(request))
        }

        return HttpBotResponse.accepted()
    }

    private fun processRequest(viberRequest: Request, requestContext: RequestContext) {
        CoroutineScope(threadPool).launch {
            viberRequest.event.toBotRequest().let { viberBotRequest ->
                val sender = viberRequest.event.sender ?: UserProfile(viberRequest.event.senderId)
                val reactions = ViberReactions(botProfile, sender, viberApi, authToken, liveChatProvider)
                botApi.process(viberBotRequest, reactions, requestContext)
            }
        }
    }

    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generatedRequest = generateRequestFromTemplate(request)
        val viberEvent = generatedRequest.asViberRequest().event as IncomingMessageEvent
        val viberInvocationRequest = ViberInvocationRequest.create(request, viberEvent) ?: return
        val reactions = ViberReactions(botProfile, viberEvent.sender, viberApi, authToken, liveChatProvider)

        botApi.process(viberInvocationRequest, reactions, requestContext)
    }

    private fun generateRequestFromTemplate(request: InvocationRequest) =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", System.currentTimeMillis().toString())

    fun initWebhook(url: String) {
        viberApi.setWebhook(url, authToken)
    }

    class Factory(
        private val channelConfig: ViberChannelConfig = ViberChannelConfig(),
    ) : JaicpCompatibleAsyncChannelFactory {

        override val channelType = "viber"

        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider,
        ): JaicpCompatibleAsyncBotChannel {
            val viberApi = ViberApi(ViberKtorClient(), apiUrl)
            val accountInfo = viberApi.accountInfo("")
            val botProfile = BotProfile(accountInfo.name)
            return ViberChannel(botApi, viberApi, botProfile, "", channelConfig).apply {
                this.liveChatProvider = liveChatProvider
            }
        }
    }

    companion object {
        private const val REQUEST_TEMPLATE_PATH = "/ViberRequestTemplate.json"
    }
}

data class ViberChannelConfig(
    var ignoreSeenEvents: Boolean = true,
    var ignoreDeliveredEvents: Boolean = true,
)
