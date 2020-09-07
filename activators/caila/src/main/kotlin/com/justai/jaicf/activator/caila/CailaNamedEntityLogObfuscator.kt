package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.logging.ConversationLogObfuscator

/**
 * Example implementation of [ConversationLogObfuscator].
 *
 * This implementation hides entities in client [BotRequest] text and uses [CailaIntentActivator] to search for entities.
 *
 * @param hideAll true to hide all entities in [BotRequest] text.
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
        getEntityTexts(activationContext).forEach { (text, entityName) ->
            out = out.replace(text, entityName)
        }
        return out
    }

    override fun obfuscateReactions(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ) = loggingContext.reactions

    private fun getEntityTexts(activationContext: ActivationContext?): Map<String, String> {
        val entities = activationContext?.activation?.context?.caila?.entities ?: return mapOf()
        return when {
            hideAll -> entities.map { it.text to it.entity }.toMap()
            !hideAll && hideCustomEntities.isEmpty() -> mapOf()
            else -> entities.filter { it.entity in hideCustomEntities }.map { it.text to it.entity }.toMap()
        }
    }
}