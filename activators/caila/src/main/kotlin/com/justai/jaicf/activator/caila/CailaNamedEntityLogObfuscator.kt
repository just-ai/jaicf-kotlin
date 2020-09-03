package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.logging.ConversationLogObfuscator

/**
 * Example implementation of [ConversationLogObfuscator].
 *
 * This implementation hides all entities found in client [BotRequest].
 * */
class CailaNamedEntityLogObfuscator(
    private val hideAll: Boolean = true,
    private val hideCustomEntities: List<String> = emptyList()
) : ConversationLogObfuscator {

    override fun obfuscateInput(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ): String {
        var out = request.input
        getEntityTexts(activationContext)?.forEach { entity ->
            out = out.replace(entity, "***")
        }
        return out
    }

    override fun obfuscateReactions(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ) = loggingContext.reactions

    private fun getEntityTexts(activationContext: ActivationContext?): List<String>? {
        val entities = activationContext?.activation?.context?.caila?.entities ?: return emptyList()
        return when {
            hideAll -> entities.map { it.text }
            !hideAll && hideCustomEntities.isEmpty() -> emptyList()
            else -> entities.filter { it.entity in hideCustomEntities }.map { it.text }
        }
    }
}