package com.justai.jaicf.context.manager

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext

/**
 * Simple in-memory [BotContextManager] implementation.
 * Stores every [BotContext] to the internal mutable map with client id as a key.
 */
object InMemoryBotContextManager : BotContextManager {
    private val storage = mutableMapOf<String, BotContext>()

    /**
     * Fetches a previously stored [BotContext] or creates a new one if it wasn't found.
     *
     * @param request current user's request
     * @return [BotContext] instance
     */
    override fun loadContext(request: BotRequest): BotContext {
        val bc = storage.computeIfAbsent(request.clientId) { clientId -> BotContext(clientId) }
        return bc.copy(dialogContext = bc.dialogContext.clone()).apply {
            result = bc.result
            client.putAll(bc.client)
            session.putAll(bc.session)
        }
    }

    /**
     * Stores a shallow copy [BotContext] to the internal mutable map.
     */
    override fun saveContext(botContext: BotContext, request: BotRequest?, response: BotResponse?) {
        storage[botContext.clientId] = botContext.copy(dialogContext = botContext.dialogContext.clone()).apply {
            result = botContext.result
            client.putAll(botContext.client)
            session.putAll(botContext.session)
        }
    }
}

private fun DialogContext.clone(): DialogContext {
    val dc = DialogContext()
    dc.nextContext = nextContext
    dc.currentContext = currentContext
    dc.nextState = nextState
    dc.currentState = currentState
    dc.transitions.putAll(transitions)
    dc.backStateStack.addAll(backStateStack)
    return dc
}