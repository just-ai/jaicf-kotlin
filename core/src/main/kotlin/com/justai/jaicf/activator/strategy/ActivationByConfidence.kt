package com.justai.jaicf.activator.strategy

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationStrategy
import com.justai.jaicf.model.state.StatePath

/**
 * Default implementation of [ActivationStrategy], used by [com.justai.jaicf.BotEngine]
 *
 * This activation strategy tries to select an activation with the greatest confidence from all children
 * of the current state, then from all siblings (including current state),
 * then from all siblings of the parent (including the parent), and so on.
 *
 * @see com.justai.jaicf.BotEngine
 * @see com.justai.jaicf.activator.Activator
 * */
object ActivationByConfidence : ActivationStrategy {

     /*
     * Selects activation by confidence using nearest states.
     *
     * @param botContext a current user's [BotContext]
     * @param activations list of all available activations
     * @return the most relevant [Activation] in terms of certain implementation of [BaseActivator]
     *
     * @see Activation
     * @see ActivationStrategy
     */
    override fun selectActivation(
        botContext: BotContext,
        activations: List<Activation>
    ): Activation {
        val first = StatePath.parse(activations.first().state!!)
        return activations.takeWhile {
            StatePath.parse(it.state!!).parent == first.parent
        }.maxBy { it.context.confidence }!!
    }
}