package com.justai.jaicf.api

/**
 * A base interface for every request to the JAICF bot.
 *
 * @property type the type of request from [BotRequestType]
 * @property clientId a user's identifier that is unique for the channel from where the request was received
 * @property input an input string that should be processed as a user's request (may contain text query, event or recognised intent)
 *
 * @see BotRequestType
 */
interface BotRequest {
    val type: BotRequestType
    val clientId: String
    val input: String
}

/**
 * The type of request.
 */
enum class BotRequestType {
    QUERY, EVENT, INTENT
}

/**
 * The request that contains a raw text query.
 * Every activator that handles query requests should use this class or create an own subclass.
 */
open class QueryBotRequest(
    override val clientId: String,
    override val input: String
): BotRequest {
    override val type = BotRequestType.QUERY
}

/**
 * The request that contains an event.
 * Every activator that handles event requests should use this class or create an own subclass.
 */
open class EventBotRequest(
    override val clientId: String,
    override val input: String
): BotRequest {
    override val type = BotRequestType.EVENT
}

/**
 * The request that contains a recognised intent.
 * Every activator that handles intent requests should use this class or create an own subclass.
 */
open class IntentBotRequest(
    override val clientId: String,
    override val input: String
): BotRequest {
    override val type = BotRequestType.INTENT
}

/**
 * Indicates if request has a raw text query.
 * @return true if request has a raw text query
 */
fun BotRequest.hasQuery() =
    this.type == BotRequestType.QUERY && input.isNotBlank()

/**
 * Indicates if request has an event.
 * @return true if request has an event
 */
fun BotRequest.hasEvent() =
    this.type == BotRequestType.EVENT && input.isNotBlank()

/**
 * Indicates if request has a recognised intent.
 * @return true if request has a recognised intent
 */
fun BotRequest.hasIntent() =
    this.type == BotRequestType.INTENT && input.isNotBlank()