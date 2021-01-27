package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.logging.ExecutionContext
import com.justai.jaicf.logging.ConversationLogObfuscator

/**
 * Hides named entities in client [BotRequest] text and uses [CailaIntentActivator] for named entity recognition.
 *
 * @property predicate to filter found entities in [ActivationContext]. If class is created with list of entities to obfuscate, predicate will be used to filter entities.
 * */
class CailaNamedEntityLogObfuscator private constructor(
    private val predicate: (String) -> Boolean
) : ConversationLogObfuscator {

    /**
     * Default constructor, which hides all named entities
     * */
    constructor() : this({ true })

    /**
     * Constructor to hide only entities with specified names
     * */
    constructor(entities: List<String>) : this({ it in entities })

    override fun obfuscateInput(executionContext: ExecutionContext): String {
        var out = executionContext.request.input
        getEntityTexts(executionContext.activationContext).forEach { (text, entityName) ->
            out = out.replace(text, entityName)
        }
        return out
    }

    private fun getEntityTexts(activationContext: ActivationContext?) =
        activationContext?.activation?.context?.caila?.entities
            ?.filter { predicate(it.entity) }
            ?.map { it.text to it.entity }?.toMap() ?: mapOf()
}
