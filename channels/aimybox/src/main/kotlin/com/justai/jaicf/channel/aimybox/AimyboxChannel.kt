package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.aimybox.api.AimyboxBotRequest
import com.justai.jaicf.channel.aimybox.api.AimyboxBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class AimyboxChannel(
    override val botApi: BotApi
): JaicpCompatibleBotChannel {

    private val JSON = Json(JsonConfiguration.Stable.copy(
        strictMode = false,
        encodeDefaults = false
    ))

    override fun process(input: String): String {
        val request = JSON.parse(AimyboxBotRequest.serializer(), input)
        val reactions = AimyboxReactions(AimyboxBotResponse(request.query))

        botApi.process(request, reactions)
        return JSON.stringify(AimyboxBotResponse.serializer(), reactions.response)
    }

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "zenbox"
        override fun create(botApi: BotApi) = AimyboxChannel(botApi)
    }
}