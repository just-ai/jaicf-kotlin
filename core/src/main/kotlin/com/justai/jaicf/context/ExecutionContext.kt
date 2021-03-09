package com.justai.jaicf.context

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.exceptions.BotException
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.Reaction

/**
 * This class will accumulate all execution information obtained during processing request.
 *
 * @see ConversationLogger
 * @see Reaction
 * */
data class ExecutionContext(
    val requestContext: RequestContext,
    var activationContext: ActivationContext?,
    val botContext: BotContext,
    val request: BotRequest,
    val firstState: String = botContext.dialogContext.currentState,
    val reactions: MutableList<Reaction> = mutableListOf(),
    val input: String = request.input
) {
    var scenarioException: BotException? = null
        internal set
}