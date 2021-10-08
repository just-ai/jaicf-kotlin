package com.justai.jaicf.model.activation

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext

interface ActivationRule {

    /**
     * Tells whether resulting [ActivatorContext] created by this rule is able to activate a scenario state.
     * This operation is short-circuit
     *
     * @param botContext current context of the bot
     * @param request current request
     * @param activator resulting activator context
     * @return `true` if this rule should be tested against given [request], `false` otherwise
     */
    fun isOnlyIf(botContext: BotContext, request: BotRequest, activator: ActivatorContext): Boolean

    /**
     * Adds the given [predicate] as a post-match condition for this rule,
     * meaning that [ActivatorContext] created by this rule will be passed to scenario
     * only if the given predicate returs true.
     *
     * @param predicate a pre-match condition
     */
    fun onlyIf(predicate: OnlyIfContext.() -> Boolean)

    open class OnlyIfContext(
        open val context: BotContext,
        open val request: BotRequest,
        open val activator: ActivatorContext
    )
}


/**
 * Base implementation of [ActivationRule] that provides support of [ActivationRule.onlyIf]
 */
abstract class ActivationRuleAdapter : ActivationRule {
    private var onlyIf: MutableList<ActivationRule.OnlyIfContext.() -> Boolean> = mutableListOf()

    override fun isOnlyIf(botContext: BotContext, request: BotRequest, activator: ActivatorContext): Boolean {
        val context = ActivationRule.OnlyIfContext(botContext, request, activator)
        return onlyIf.all { it.invoke(context) }
    }

    override fun onlyIf(predicate: ActivationRule.OnlyIfContext.() -> Boolean) {
        onlyIf.add(predicate)
    }
}
