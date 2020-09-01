package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.aimybox.api.*
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.context.RequestContext

class AimyboxChannel(
    override val botApi: BotApi,
    private val apiKey: String? = null
) : JaicpCompatibleBotChannel {

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val req = JSON.parse(AimyboxBotRequest.serializer(), request.receiveText())

        if (!apiKey.isNullOrEmpty() && apiKey != req.key) {
            return null
        }

        val reactions = AimyboxReactions(AimyboxBotResponse(req.query))

        botApi.process(req, reactions, RequestContext(newSession = req.query.isEmpty(), httpBotRequest = request))
        return JSON.stringify(AimyboxBotResponse.serializer(), reactions.response).asJsonHttpBotResponse()
    }

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "zenbox"
        override fun create(botApi: BotApi) = AimyboxChannel(botApi)
    }
}