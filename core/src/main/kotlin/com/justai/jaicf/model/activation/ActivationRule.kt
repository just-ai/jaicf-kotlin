package com.justai.jaicf.model.activation

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext

interface ActivationRule {

    /**
     * Tells whether this rule is able to activate given [request]
     *
     * @param botContext current context of the bot
     * @param request current request
     * @return `true` if this rule should be tested against given [request], `false` otherwise
     */
    fun canHandle(botContext: BotContext, request: BotRequest): Boolean

    /**
     * Sets the given [predicate] as a pre-match condition for this rule,
     * meaning that this rule will be tested by activator only if the given predicate returs true.
     *
     * @param predicate a pre-match condition
     */
    fun onlyIf(predicate: OnlyIfContext.() -> Boolean)

    data class OnlyIfContext(val context: BotContext, val request: BotRequest)
}


/**
 * Base implementation of [ActivationRule] that provides support of [ActivationRule.onlyIf]
 */
abstract class ActivationRuleAdapter : ActivationRule {
    private var onlyIf: ActivationRule.OnlyIfContext.() -> Boolean = { true }

    override fun canHandle(botContext: BotContext, request: BotRequest): Boolean {
        return onlyIf.invoke(ActivationRule.OnlyIfContext(botContext, request))
    }

    override fun onlyIf(predicate: ActivationRule.OnlyIfContext.() -> Boolean) {
        onlyIf = predicate
    }
}
