package com.justai.jaicf.channel.invocationapi

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest

val BotRequest.invocationApi get() = this as? InvocationRequest

/**
 * A request processed by [InvocableBotChannel] implementation.
 *
 * @property clientId inherited from [BotRequest] is a recipient or channel identifier from a concrete channel implementation.
 * @property input inherited from [BotRequest] a input (text or event) sent via [InvocableBotChannel].
 * @property requestData a stringified data sent with request.
 * */
interface InvocationRequest : BotRequest {
    val requestData: String
}

/**
 * An [EventBotRequest] sent via [InvocableBotChannel].
 *
 * @property clientId inherited from [BotRequest] is a recipient or channel identifier from a concrete channel implementation.
 * @property input inherited from [BotRequest] a input (text or event) sent via [InvocableBotChannel].
 * @property requestData a stringified data sent with request.
 * */
open class InvocationEventRequest(
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : InvocationRequest, EventBotRequest(clientId, input)

/**
 * A [QueryBotRequest] sent via [InvocableBotChannel].
 *
 * @property clientId inherited from [BotRequest] is a recipient or channel identifier from a concrete channel implementation.
 * @property input inherited from [BotRequest] a input (text or event) sent via [InvocableBotChannel].
 * @property requestData a stringified data sent with request.
 * */
open class InvocationQueryRequest(
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : InvocationRequest, QueryBotRequest(clientId, input)
