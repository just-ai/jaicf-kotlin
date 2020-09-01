package com.justai.jaicf.context

import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.reactions.Reaction
import com.justai.jaicf.logging.ConversationLogger

/**
 * Internal class used by [ConversationLogger] implementations.
 * This class will accumulate all supported reactions for single requests, also containing [HttpBotRequest] if possible.
 * This information is further passed to [ConversationLogger] to perform logging.
 *
 * @see ConversationLogger
 * @see Reaction
 * */
data class LoggingContext(
    val httpBotRequest: HttpBotRequest? = null,
    val reactions: MutableList<Reaction> = mutableListOf()
)