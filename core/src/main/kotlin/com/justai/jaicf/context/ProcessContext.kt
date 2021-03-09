package com.justai.jaicf.context

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.reactions.Reactions

/**
 * An internal class that holds everything about current invocation of bot engine process method.
 */
data class ProcessContext(
    val request: BotRequest,
    val reactions: Reactions,
    val requestContext: RequestContext,
    val botContext: BotContext,
    val activationContext: ActivationContext,
    val executionContext: ExecutionContext
)