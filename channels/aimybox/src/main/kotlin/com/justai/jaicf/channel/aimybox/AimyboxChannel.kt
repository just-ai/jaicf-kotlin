package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.aimybox.api.*
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.context.RequestContext

class AimyboxChannel(
    override val botApi: BotApi,
    private val apiKey: String? = null
): JaicpCompatibleBotChannel {

    override fun process(input: String): String? {
        val request = JSON.parse(AimyboxBotRequest.serializer(), input)

        if (!apiKey.isNullOrEmpty() && apiKey != request.key) {
            return null
        }

        val reactions = AimyboxReactions(AimyboxBotResponse(request.query))

        botApi.process(request, reactions, RequestContext(newSession = request.query.isEmpty()))
        return JSON.stringify(AimyboxBotResponse.serializer(), reactions.response)
    }

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "zenbox"
        override fun create(botApi: BotApi) = AimyboxChannel(botApi)
    }
}