package com.justai.jaicf.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext

/**
 * A context used for defining static route conditions.
 *
 * @param request current user's request
 * @param requestContext current channel request's context
 * @param context current client's bot context
 * @param routables map of [BotEngine] with string name identifiers
 *
 * @see BotRoutingEngine
 * */
data class BotRoutingStaticBuilder(
    val request: BotRequest,
    val requestContext: RequestContext,
    val context: BotContext,
    val routables: Map<String, BotEngine>,
)