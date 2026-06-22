package com.justai.jaicf.channel.max

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.max.api.MaxBotApi
import com.justai.jaicf.channel.max.dto.MaxUpdate
import com.justai.jaicf.channel.max.dto.maxObjectMapper
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger

class MaxChannel(
    override val botApi: BotApi,
    // null in JAICP-proxied mode (JAICP cloud holds the bot auth); set for standalone
    // direct Max API use. The outbound auth model is finalized in VS-13663.
    private val maxBotToken: String? = null,
    private val maxApiUrl: String = DEFAULT_API_URL
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel, WithLogger, java.io.Closeable {

    private val api = MaxBotApi(maxBotToken, maxApiUrl)
    private var liveChatProvider: JaicpLiveChatProvider? = null

    override fun process(request: HttpBotRequest): HttpBotResponse {
        // Always acknowledge the JAICP webhook with 202; processing errors are logged, never surfaced as 500.
        try {
            val update = maxObjectMapper.readValue(request.receiveText(), MaxUpdate::class.java)
            val botRequest = update.toBotRequest()
            if (botRequest == null) {
                logger.debug("No request converter for Max update of type ${update::class.simpleName}")
            } else {
                botApi.process(
                    botRequest,
                    MaxReactions(api, botRequest, liveChatProvider),
                    RequestContext.fromHttp(request)
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to process Max update", e)
        }
        return HttpBotResponse.accepted()
    }

    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generated = getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
        val update = maxObjectMapper.readValue(generated, MaxUpdate::class.java)
        val botRequest = update.toBotRequest() ?: return
        botApi.process(botRequest, MaxReactions(api, botRequest, liveChatProvider), requestContext)
    }

    override fun close() = api.close()

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "max"
        private const val DEFAULT_API_URL = "https://botapi.max.ru"
        private const val REQUEST_TEMPLATE_PATH = "/MaxRequestTemplate.json"

        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ): JaicpCompatibleAsyncBotChannel =
            MaxChannel(botApi, maxApiUrl = apiUrl).apply {
                this.liveChatProvider = liveChatProvider
            }
    }
}
