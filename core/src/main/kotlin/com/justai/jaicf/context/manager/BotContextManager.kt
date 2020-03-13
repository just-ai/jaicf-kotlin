package com.justai.jaicf.context.manager

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
     * @param clientId current user's id who made a request to the bot
     * @return current user's [BotContext]
     */
    fun loadContext(clientId: String): BotContext

    /**
     * Persists the current user's bot context.
     * @param botContext an instance of BotContext to persist
     */
    fun saveContext(botContext: BotContext)
}