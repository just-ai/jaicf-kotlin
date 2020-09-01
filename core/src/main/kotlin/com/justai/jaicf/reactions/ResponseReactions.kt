package com.justai.jaicf.reactions

import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.model.state.StatePath

/**
 * A base abstraction for reactions that hold a particular channel-related [BotResponse] that could be filled with some replies.
 * Used mostly by synchronous channels like Aimybox, Alexa or Google Assistant that respond wih single response on every request.
 *
 * @property response a channel-related response that should be filled by reactions methods
 *
 * @see [Reactions]
 * @see [BotResponse]
 */
abstract class ResponseReactions<TResponse : BotResponse>(
    open val response: TResponse
) : Reactions()