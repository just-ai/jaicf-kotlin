package com.justai.jaicf.model.activation

import com.justai.jaicf.context.BotContext

/**
 * Abstraction to select most relevant [Activation]
 * */
interface ActivationSelector {

    /**
     * Selects most relevant [Activation]
     *
     * @param botContext a current user's [BotContext]
     * @param activations list of all available activations
     * @return most relevant [Activation]
     *
     * @see Activation
     * @see com.justai.jaicf.activator.selection.ActivationByConfidence
     * @see com.justai.jaicf.activator.selection.ActivationByContextPenalty
     */
    fun selectActivation(botContext: BotContext, activations: List<Activation>): Activation
}