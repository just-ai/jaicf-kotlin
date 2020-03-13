package com.justai.jaicf.channel.alexa

import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.util.JacksonSerializer
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory

class AlexaChannel(
    override val botApi: BotApi
) : JaicpCompatibleBotChannel {

    private val serializer = JacksonSerializer()
    private val skill = AlexaSkill.create(botApi)

    override fun process(input: String): String {
        val request = serializer
            .deserialize<RequestEnvelope>(input, RequestEnvelope::class.java)

        val response = skill.invoke(request)
        return serializer.serialize(response)
    }

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "alexa"
        override fun create(botApi: BotApi) = AlexaChannel(botApi)
    }
}