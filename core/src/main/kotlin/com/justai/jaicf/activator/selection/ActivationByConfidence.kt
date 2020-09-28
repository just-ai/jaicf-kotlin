package com.justai.jaicf.activator.selection

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationSelector
import com.justai.jaicf.model.state.StatePath

/**
 * This activation selector tries to select an activation with the greatest confidence from all children
 * of the current state, then from all siblings (including current state),
 * then from all siblings of the parent (including the parent), and so on.
 *
 * @see com.justai.jaicf.BotEngine
 * @see com.justai.jaicf.activator.Activator
 * */
class ActivationByConfidence : ActivationSelector {

    /**
     * @param botContext a current user's [BotContext]
     * @param activations list of all available activations
     * @return the most relevant [Activation]
     *
     * @see Activation
     * @see ActivationSelector
     */
    override fun selectActivation(botContext: BotContext, activations: List<Activation>): Activation {
        return activations
            .groupBy { it.transition?.distance }.entries.first().value
            .maxBy { it.context.confidence }!!
    }
}
