package com.justai.jaicf.context.manager

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext

/**
 * Simple in-memory [BotContextManager] implementation.
 * Stores every [BotContext] to the internal mutable map with client id as a key.
 */
object InMemoryBotContextManager: BotContextManager {
    private val storage = mutableMapOf<String, BotContext>()

    /**
     * Fetches a previously stored [BotContext] or creates a new one if it wasn't found.
     *
     * @param clientId a client identifier
     * @return [BotContext] instance
     */
    override fun loadContext(request: BotRequest): BotContext {
        storage.putIfAbsent(request.clientId, BotContext(request.clientId))
        return storage[request.clientId]!!
    }

    /**
     * Stores a shallow copy [BotContext] to the internal mutable map.
     */
    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        storage[botContext.clientId] = botContext.copy().apply {
            result = botContext.result
            client.putAll(botContext.client)
            session.putAll(botContext.session)
        }
    }

}