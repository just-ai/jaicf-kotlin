package com.justai.jaicf.channel.jaicp.gateway

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.gateway.BotGateway
import com.justai.jaicf.gateway.BotGatewayEventRequest

internal object BotGatewayRequestAdapter {

    /**
     * Ensures jaicp bot gateway request processing.
     *
     * @return true is request is [BotGatewayEventRequest] and it is processed
     *
     * @see BotGateway
     * @see BotGatewayEventRequest
     * */
    fun ensureGatewayRequest(channel: BotGateway, request: JaicpBotRequest): Boolean {
        val event = request.event ?: return false
        if (!request.isGatewayRequest()) {
            return false
        }
        val data = try {
            JSON.decodeFromString(BotGatewayRequestData.serializer(), request.raw)
        } catch (e: Exception) {
            return false
        }
        channel.processGatewayRequest(
            request = BotGatewayEventRequest(data.chatId, event, request.raw),
            requestContext = RequestContext.fromHttp(request.asHttpBotRequest())
        )
        return true
    }
}