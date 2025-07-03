package com.justai.jaicf.activator.selection

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.transition.Transition

/**
 * Selects the most relevant activation, based on context specificity first and then confidence.
 *
 * Tries to select the most confident transition from current state to some of its child,
 * if no children available tries to stay in the same state, otherwise tries to select best transition
 * available from scenario root.
 */
class ContextFirstActivationSelector : ActivationSelector {
    override fun selectActivation(
        botContext: BotContext,
        activations: List<Pair<Transition, ActivatorContext>>
    ): Activation? {
        val current = botContext.dialogContext.currentContext

        val (transition, activatorContext) = activations.sortedWith(
            compareByDescending<Pair<Transition, ActivatorContext>> {
                it.first.fromState.commonPrefixWith(current).length
            }.thenByDescending {
                it.second.confidence
            }.thenByDescending {
                it.first.toState == current
            }
        ).firstOrNull() ?: return null

        return Activation(transition.toState, activatorContext)
    }
}