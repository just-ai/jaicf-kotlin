package com.justai.jaicf.context

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.reactions.Reactions

/**
 * An internal class that holds everything about current invocation of bot engine process method.
 */
data class ProcessContext(
    val request: BotRequest,
    val reactions: Reactions,
    val requestContext: RequestContext,
    val botContext: BotContext,
    val activation: Activation,
    val skippedActivators: List<ActivatorContext>
)