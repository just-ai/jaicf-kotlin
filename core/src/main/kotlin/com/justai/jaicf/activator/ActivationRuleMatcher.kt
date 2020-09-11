package com.justai.jaicf.activator

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.model.activation.ActivationRule

/**
 * Represents a transition rule matcher.
 *
 * Every transition in scenario graph has its own [ActivationRule], so [ActivationRuleMatcher] must determine
 * either it can activate certain transition and provide corresponding non-null [ActivatorContext] or it can't.
 *
 * @see [StateMapActivator.provideRuleMatcher]
 * @see [StateMapActivator.ruleMatcher]
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
}
