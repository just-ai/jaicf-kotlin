package com.justai.jaicf.logging

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.reactions.Reactions

/**
 * Internal class used by [ConversationLogger] implementations.
 * This class will accumulate all reactions returned by scenario, also containing [HttpBotRequest] if possible.
 * This information is further passed to [ConversationLogger] to perform logging.
 *
 * @see ConversationLogger
 * @see Reaction
 * */
data class LoggingContext(
    val requestContext: RequestContext,
    var activationContext: ActivationContext?,
    val botContext: BotContext,
    val request: BotRequest,
    val firstState: String = botContext.dialogContext.currentState,
    val reactions: MutableList<Reaction> = mutableListOf(),
    val input: String = request.input,
    var channelReactions: Reactions? = null
)