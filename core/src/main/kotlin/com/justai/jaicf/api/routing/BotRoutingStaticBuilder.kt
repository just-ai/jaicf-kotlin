package com.justai.jaicf.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.reactions.Reactions

/**
 * JAVADOC ME
 * */
data class BotRoutingStaticBuilder(
    val request: BotRequest,
    val reactions: Reactions,
    val requestContext: RequestContext,
    val contextManager: BotContextManager?,
    val routables: Map<String, BotEngine>
)