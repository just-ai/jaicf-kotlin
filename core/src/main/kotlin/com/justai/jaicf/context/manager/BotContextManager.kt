package com.justai.jaicf.context.manager

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext

/**
 * Main abstraction for transparent [BotContext] persistence.
 * An implementation should provide methods for fetching and storing of the current user's context.
 * You can find multiple implementations of this interface in SDK modules: InMemory, Mongo, MapDB and others.
 *
 * @see BotContext
 * @see InMemoryBotContextManager
 */
interface BotContextManager {

    /**
     * Loads the bot context from the storage
     *
     * @param request current user's request
     * @return current user's [BotContext]
     */
    fun loadContext(request: BotRequest): BotContext

    /**
     * Persists the current user's bot context.
     *
     * @param botContext an instance of BotContext to persist
     * @param request current user's request
     * @param response a response that are going to be sent back to the user
     */
    fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?)
}