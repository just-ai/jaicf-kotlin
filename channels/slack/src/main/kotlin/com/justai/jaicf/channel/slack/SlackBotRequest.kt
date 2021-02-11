package com.justai.jaicf.channel.slack

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.gateway.BotGatewayEventRequest
import com.justai.jaicf.gateway.BotGatewayQueryRequest
import com.justai.jaicf.gateway.BotGatewayRequest
import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload
import com.slack.api.model.event.Event
import com.slack.api.model.event.MessageEvent

val BotRequest.slack
    get() = this as? SlackBotRequest

val SlackBotRequest.event
    get() = this as? SlackEventRequest

val SlackBotRequest.command
    get() = this as? SlackCommandRequest

val SlackBotRequest.action
    get() = this as? SlackActionRequest


interface SlackBotRequest: BotRequest

data class SlackEventRequest(
    val payload: EventsApiPayload<out Event>
): SlackBotRequest {

    override val clientId = when(payload.event) {
        is MessageEvent -> (payload.event as MessageEvent).user
        else -> payload.teamId
    }

    override val type = when(payload.event) {
        is MessageEvent -> BotRequestType.QUERY
        else -> BotRequestType.EVENT
    }

    override val input = when(payload.event) {
        is MessageEvent -> (payload.event as MessageEvent).text
        else -> payload.event.type
    }
}

data class SlackCommandRequest(
    val payload: SlashCommandPayload
): SlackBotRequest, QueryBotRequest(
    clientId = payload.userId,
    input = payload.command
)

data class SlackActionRequest(
    val payload: BlockActionPayload
): SlackBotRequest, QueryBotRequest(
    clientId = payload.user.id,
    input = payload.actions[0].value
)

interface SlackGatewayRequest : SlackBotRequest, BotGatewayRequest {
    companion object {
        fun create(r: BotGatewayRequest) = when (r) {
            is BotGatewayEventRequest -> SlackGatewayEventRequest(r.clientId, r.input, r.requestData)
            is BotGatewayQueryRequest -> SlackGatewayQueryRequest(r.clientId, r.input, r.requestData)
            else -> null
        }
    }
}

data class SlackGatewayEventRequest(
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : SlackGatewayRequest, BotGatewayEventRequest(clientId, input, requestData)

data class SlackGatewayQueryRequest(
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : SlackGatewayRequest, BotGatewayQueryRequest(clientId, input, requestData)
