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
import com.justai.jaicf.channel.max.dto.MaxObjectMapper
import com.justai.jaicf.channel.max.dto.MaxUpdate
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.logging.WithLogger

class MaxChannel(
    override val botApi: BotApi,
    private val maxBotToken: String,
    private val maxApiUrl: String = "https://botapi.max.ru/"
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel, WithLogger {

    private val api = MaxBotApi(maxBotToken, maxApiUrl)
    private var liveChatProvider: JaicpLiveChatProvider? = null

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val update = MaxObjectMapper.mapper.readValue(request.receiveText(), MaxUpdate::class.java)
        val botRequest = MaxBotRequestFactory.create(update)
        if (botRequest == null) {
            logger.debug("No request converter for Max update: $update")
            return HttpBotResponse.accepted()
        }
        botApi.process(
            botRequest,
            MaxReactions(api, botRequest, liveChatProvider),
            RequestContext.fromHttp(request)
        )
        return HttpBotResponse.accepted()
    }

    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generated = getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
        val update = MaxObjectMapper.mapper.readValue(generated, MaxUpdate::class.java)
        val botRequest = MaxBotRequestFactory.create(update) ?: return
        botApi.process(botRequest, MaxReactions(api, botRequest, liveChatProvider), requestContext)
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "max"
        private const val REQUEST_TEMPLATE_PATH = "/MaxRequestTemplate.json"

        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ): JaicpCompatibleAsyncBotChannel =
            MaxChannel(botApi, maxBotToken = "", maxApiUrl = apiUrl).apply {
                this.liveChatProvider = liveChatProvider
            }
    }
}
