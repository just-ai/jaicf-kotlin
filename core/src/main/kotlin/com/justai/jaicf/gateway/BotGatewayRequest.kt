package com.justai.jaicf.gateway

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest

val BotRequest.gateway get() = this as? BotGatewayRequest

/**
 * A request processed by [BotGateway] implementation.
 *
 * @property clientId inherited from [BotRequest] is a recipient or channel identifier from a concrete channel implementation.
 * @property input inherited from [BotRequest] a input (text or event) sent via gateway.
 * @property requestData a stringified data sent with request.
 * */
interface BotGatewayRequest : BotRequest {
    val requestData: String
}

/**
 * An [EventBotRequest] sent via [BotGateway].
 *
 * @property clientId inherited from [BotRequest] is a recipient or channel identifier from a concrete channel implementation.
 * @property input inherited from [BotRequest] a input (text or event) sent via gateway.
 * @property requestData a stringified data sent with request.
 * */
open class BotGatewayEventRequest(
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : BotGatewayRequest, EventBotRequest(clientId, input)

/**
 * A [QueryBotRequest] sent via [BotGateway].
 *
 * @property clientId inherited from [BotRequest] is a recipient or channel identifier from a concrete channel implementation.
 * @property input inherited from [BotRequest] a input (text or event) sent via gateway.
 * @property requestData a stringified data sent with request.
 * */
open class BotGatewayQueryRequest(
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : BotGatewayRequest, QueryBotRequest(clientId, input)
