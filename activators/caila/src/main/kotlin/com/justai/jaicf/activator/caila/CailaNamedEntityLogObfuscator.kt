package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.logging.ConversationLogObfuscator

/**
 * Hides named entities in client [BotRequest] text and uses [CailaIntentActivator] for named entity recognition.
 * */
class CailaNamedEntityLogObfuscator private constructor(
    private val hideAll: Boolean = true,
    private val entityNames: List<String> = emptyList()
) : ConversationLogObfuscator {

    /**
     * Default constructor, which hides all named entities
     * */
    constructor() : this(true, emptyList())

    /**
     * Constructor to hide only entities with specified names
     * */
    constructor(entities: List<String>) : this(false, entities)

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
        val nerEntities = activationContext?.activation?.context?.caila?.entities ?: return mapOf()
        return when (hideAll) {
            true -> nerEntities.map { it.text to it.entity }.toMap()
            false -> nerEntities.filter { it.entity in entityNames }.map { it.text to it.entity }.toMap()
        }
    }
}
