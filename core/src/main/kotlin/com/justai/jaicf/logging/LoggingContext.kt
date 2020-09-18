package com.justai.jaicf.logging

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.context.BotContext

/**
 * Internal class used by [ConversationLogger] implementations.
 * This class will accumulate all supported reactions for single requests, also containing [HttpBotRequest] if possible.
 * This information is further passed to [ConversationLogger] to perform logging.
 *
 * @see ConversationLogger
 * @see LoggingReaction
 * */
data class LoggingContext(
    val httpBotRequest: HttpBotRequest? = null,
    val activationContext: ActivationContext?,
    val botContext: BotContext,
    val request: BotRequest,
    val firstState: String = botContext.dialogContext.currentState,
    val reactions: MutableList<LoggingReaction> = mutableListOf(),
    val input: String = request.input
)