package com.justai.jaicf.activator.selection

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.transition.Transition

/**
 * A selector, that is used for selecting the most relevant [Activation] from all possible.
 *
 * @see [com.justai.jaicf.activator.Activator]
 * @see [ContextFirstActivationSelector]
 * @see [ContextRankingActivationSelector]
 */
interface ActivationSelector {

    /**
     * Receives a list of all possible transitions and selects the most relevant one.
     *
     * @param botContext a current [BotContext].
     * @param activations a list of pairs of [Transition] and [ActivatorContext] to choose from.
     *
     * @return [Activation] that is built from the most relevant transition and context,
     * or `null` if no [Activation] should be selected.
     */
    fun selectActivation(
        botContext: BotContext,
        activations: List<Pair<Transition, ActivatorContext>>
    ): Activation?

    companion object {
        val default = ContextFirstActivationSelector()
    }
}

val Transition.isFromRoot get() = fromState == "/"
fun Transition.isFrom(from: String) = fromState == from
fun Transition.isTo(to: String) = toState == to
