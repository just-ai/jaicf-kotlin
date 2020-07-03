package com.justai.jaicf.api

import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.reactions.Reactions

/**
 * The main API of bot engine.
 * Implementation of this API processes requests to the bot using the scenario model and current bot context.
 *
 * @see com.justai.jaicf.BotEngine
 * @see com.justai.jaicf.context.BotContext
 */
interface BotApi {

    /**
     * Processes the request from the particular channel to the bot.
     * It doesn't return any result because channel-related [Reactions] implements response building and sending.
     *
     * @param request request from the particular channel
     * @param reactions abstraction that provides all channel-related API to build and send a response(s)
     * @param requestContext additional general request's data that can be used during the request processing
     * @param contextManager a [BotContextManager] that can override the default one configured for this [com.justai.jaicf.BotEngine]
     *
     * @see BotRequest
     * @see Reactions
     * @see RequestContext
     */
    fun process(
        request: BotRequest,
        reactions: Reactions,
        requestContext: RequestContext = RequestContext(),
        contextManager: BotContextManager? = null
    )
}