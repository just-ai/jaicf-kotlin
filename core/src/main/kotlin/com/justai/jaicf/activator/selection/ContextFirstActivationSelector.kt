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

        val toChildren = activations.filter { it.first.isFrom(current) }.maxBy { it.second.confidence }
        val toCurrent = activations.filter { it.first.isTo(current) }.maxBy { it.second.confidence }
        val fromRoot = activations.filter { it.first.isFromRoot }.maxBy { it.second.confidence }

        val best = toChildren ?: toCurrent ?: fromRoot
        return best?.let { Activation(it.first.toState, it.second) }
    }

}