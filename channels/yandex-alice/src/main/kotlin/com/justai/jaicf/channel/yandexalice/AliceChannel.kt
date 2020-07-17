package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.manager.AliceBotContextManager
import com.justai.jaicf.context.RequestContext

class AliceChannel(
    override val botApi: BotApi,
    private val oauthToken: String? = null,
    useDataStorage: Boolean = false
) : JaicpCompatibleBotChannel {

    private val contextManager = useDataStorage.takeIf { it }?.let { AliceBotContextManager() }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = JSON.parse(AliceBotRequest.serializer(), request.receiveText())
        val botResponse = AliceBotResponse(botRequest)

        if (botRequest.request?.originalUtterance == "ping") {
            botResponse.response?.text = "pong"
        } else {
            botRequest.headers.putAll(request.headers)
            val api = oauthToken?.let { AliceApi(oauthToken, botRequest.session.skillId) }
            val reactions = AliceReactions(api, botRequest, botResponse)
            botApi.process(
                request = botRequest,
                reactions = reactions,
                contextManager = contextManager,
                requestContext = RequestContext(newSession = botRequest.session.newSession)
            )
        }

        return JSON.stringify(AliceBotResponse.serializer(), botResponse).asJsonHttpBotResponse()
    }

    class Factory(
        private val oauthToken: String? = null
    ) : JaicpCompatibleChannelFactory {
        override val channelType = "yandex"
        override fun create(botApi: BotApi) = AliceChannel(botApi, oauthToken)
    }
}