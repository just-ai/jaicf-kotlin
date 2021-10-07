@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.justai.jaicf.activator

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.ActivationRule

/**
 * Represents a transition rule matcher.
 *
 * Every transition in scenario graph has its own [ActivationRule], so [ActivationRuleMatcher] must determine
 * either it can activate certain transition and provide corresponding non-null [ActivatorContext] or it can't.
 *
 * @see [BaseActivator.provideRuleMatcher]
 * @see [BaseActivator.ruleMatcher]
 */
interface ActivationRuleMatcher {
    /**
     * Attempts to match the [rule] and provide [ActivatorContext].
     *
     * @param rule transition rule to be tested.
     *
     * @return non-null [ActivatorContext] on successful match, `null` otherwise.
     *
     * @see [ActivationRule]
     * @see [ActivatorContext]
     */
    fun match(rule: ActivationRule): ActivatorContext?

    /**
     * Attempts to match the [rule] and provide [ActivatorContext].
     * Tests [ActivationRule.onlyIf] first, then calls [ActivationRuleMatcher.match]
     *
     * @param botContext current context
     * @param request current request
     * @param rule transition rule to be tested.
     *
     * @return non-null [ActivatorContext] on successful match, `null` otherwise.
     *
     * @see [ActivationRule]
     * @see [ActivatorContext]
     */
    fun match(botContext: BotContext, request: BotRequest, rule: ActivationRule): ActivatorContext? {
        if (!rule.canHandle(botContext, request)) return null
        return match(rule)
    }
}
