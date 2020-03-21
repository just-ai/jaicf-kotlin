package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Card
import com.justai.jaicf.channel.yandexalice.api.model.Image
import com.justai.jaicf.channel.yandexalice.api.model.ItemsList
import com.justai.jaicf.context.RequestContext
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

class AliceChannel(
    override val botApi: BotApi,
    private val oauthToken: String? = null
) : JaicpCompatibleBotChannel {

    private val JSON = Json(
        configuration = JsonConfiguration.Stable.copy(
            strictMode = false,
            classDiscriminator = "type"),

        context = SerializersModule {
            polymorphic(Card::class) {
                Image::class with Image.serializer()
                ItemsList::class with ItemsList.serializer()
            }
        }
    )

    override fun process(input: String): String? {
        val request = JSON.parse(AliceBotRequest.serializer(), input)
        val response = AliceBotResponse(request)

        if (request.request.originalUtterance == "ping") {
            response.response.text = "pong"
        } else {
            val api = oauthToken?.let { AliceApi(oauthToken, request.session.skillId) }
            val reactions = AliceReactions(api, request, response)
            botApi.process(request, reactions, RequestContext(newSession = request.session.newSession))
        }

        return JSON.stringify(AliceBotResponse.serializer(), response)
    }

    class Factory(
        private val oauthToken: String? = null
    ) : JaicpCompatibleChannelFactory {
        override val channelType = "yandex"
        override fun create(botApi: BotApi) = AliceChannel(botApi, oauthToken)
    }
}