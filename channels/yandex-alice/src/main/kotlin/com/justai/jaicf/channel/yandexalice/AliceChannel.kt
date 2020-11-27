package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelWithApiClient
import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.manager.AliceBotContextManager
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.kotlin.ifTrue

class AliceChannel(
    override val botApi: BotApi,
    private val oauthToken: String? = null,
    useDataStorage: Boolean = false
) : JaicpCompatibleChannelWithApiClient {

    companion object {
        private const val DEFAULT_ALICE_API_URL = "https://dialogs.yandex.net/api/v1"
    }

    private var aliceApiUrl = DEFAULT_ALICE_API_URL
    private val contextManager = useDataStorage.ifTrue { AliceBotContextManager() }

    override fun configureApiUrl(proxyUrl: String) {
        aliceApiUrl = proxyUrl
    }

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = JSON.decodeFromString(AliceBotRequest.serializer(), request.receiveText())
        val botResponse = AliceBotResponse(botRequest)

        if (botRequest.request?.originalUtterance == "ping") {
            botResponse.response?.text = "pong"
        } else {
            botRequest.headers.putAll(request.headers)
            val api = oauthToken?.let { AliceApi(oauthToken, botRequest.session.skillId, aliceApiUrl) }
            val reactions = AliceReactions(api, botRequest, botResponse)
            botApi.process(
                request = botRequest,
                reactions = reactions,
                contextManager = contextManager,
                requestContext = RequestContext(newSession = botRequest.session.newSession, httpBotRequest = request)
            )
        }

        return JSON.encodeToString(AliceBotResponse.serializer(), botResponse).asJsonHttpBotResponse()
    }

    class Factory(private val useDataStorage: Boolean = false) : JaicpCompatibleChannelFactory {
        override val channelType = "yandex"
        override fun create(botApi: BotApi) = AliceChannel(botApi, oauthToken = "", useDataStorage = useDataStorage)
    }
}