package com.justai.jaicf.channel.alexa

import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.util.JacksonSerializer
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory

class AlexaChannel(
    override val botApi: BotApi
) : JaicpCompatibleBotChannel {

    private val serializer = JacksonSerializer()
    private val skill = AlexaSkill.create(botApi)

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = serializer.deserialize(request.receiveText(), RequestEnvelope::class.java)
        val botResponse = skill.invoke(botRequest, request)
        return serializer.serialize(botResponse).asJsonHttpBotResponse()
    }

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "alexa"
        override fun create(botApi: BotApi) = AlexaChannel(botApi)
    }
}