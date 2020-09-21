package com.justai.jaicf.model.activation

import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.context.BotContext

/**
 * Abstraction for strategy, using which activation will be selected from list.
 * */
interface ActivationStrategy {

    /**
     * Selects [Activation] from list using implemented strategy.
     *
     * @param botContext a current user's [BotContext]
     * @param activations list of all available activations
     * @return the most relevant [Activation] in terms of certain implementation of [BaseActivator]
     *
     * @see Activation
     * @see com.justai.jaicf.activator.strategy.ActivationByConfidence
     * @see com.justai.jaicf.activator.strategy.ActivationByContextPenalty
     */
    fun selectActivation(
        botContext: BotContext,
        activations: List<Activation>
    ): Activation
}
