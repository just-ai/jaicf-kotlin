package com.justai.jaicf.channel

import com.justai.jaicf.api.BotApi

/**
 * A base interface for every channel that receives a requests to the JAICF bot.
 * Should process a request using a provided [BotApi] implementation.
 * Every channel should create it's own instances of [com.justai.jaicf.api.BotRequest], [com.justai.jaicf.api.BotResponse], [com.justai.jaicf.reactions.Reactions] and pass it to the bot engine process method once a new request was received.
 * There SDK already contains ready to use helper subclasses of this interface that describes a contract for different types of channels.
 *
 * @property botApi a bot engine that should process requests to this channel
 *
 * @see BotApi
 * @see ConsoleChannel
 * @see com.justai.jaicf.api.BotRequest
 * @see com.justai.jaicf.api.BotResponse
 * @see com.justai.jaicf.reactions.Reactions
 * @see com.justai.jaicf.channel.http.HttpBotChannel
 * @see com.justai.jaicf.channel.jaicp.JaicpBotChannel
 */
interface BotChannel {
    val botApi: BotApi
}