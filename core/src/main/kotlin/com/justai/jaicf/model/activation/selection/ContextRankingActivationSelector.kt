package com.justai.jaicf.model.activation.selection

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.transition.Transition

/**
 * Selects the most relevant activation, based on confidence weighting.
 *
 * Default implementation multiplies activation confidence by a factor that is different
 * for different layers of scenario graph. More specific transition means greater factor.
 * Specificity is ordered as follows:
 * 1. Children of the current state;
 * 2. The current state itself;
 * 3. States available from the root of scenario.
 */
open class ContextRankingActivationSelector : ActivationSelector {
    override fun selectActivation(
        botContext: BotContext,
        activations: List<Pair<Transition, ActivatorContext>>
    ): Activation? {
        return activations.maxBy {
            calculateScore(botContext, it.first, it.second)
        }?.let { Activation(it.first.toState, it.second) }
    }


    /**
     * Calculates the score of the given [Transition] and given [ActivatorContext].
     * One can override this method and implement its own scoring mechanism.
     */
    protected open fun calculateScore(
        botContext: BotContext,
        transition: Transition,
        context: ActivatorContext
    ): Float {
        val current = botContext.dialogContext.currentContext

        val factor = when {
            transition.isFrom(current) -> 1f
            transition.isTo(current) -> 0.8f
            transition.isFromRoot -> 0.6f
            else -> 0f
        }
        
        return context.confidence * factor
    }
}