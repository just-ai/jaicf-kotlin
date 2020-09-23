package com.justai.jaicf.model.activation

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.state.StatePath

/**
 * Abstraction to select most relevant [Activation]
 * */
abstract class ActivationSelector {

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
    abstract fun selectActivation(botContext: BotContext, activations: List<Activation>): Activation

    /**
     * Sorts activations by context difference - number of transitions between current state and activation target state.
     * */
    protected fun Collection<Activation>.sortedByContext(currentState: StatePath) = sortedBy { activation ->
        activation.state?.let { targetState ->
            calculateStatesDifference(StatePath.parse(targetState), currentState)
        }
    }

    /**
     * Calculates context difference (number of transitions) between targetState and currentState
     * e.g.: currentState = "/root/child1/child2/child3", targetState = "/root/child1"; diff level = 2
     *
     * @param targetState possible target state by activation
     * @param currentContext current context
     *
     * @return number of transitions between current and target state
     * */
    protected fun calculateStatesDifference(targetState: StatePath, currentContext: StatePath): Int {
        val toState = targetState.components
        val fromState = currentContext.components
        return fromState.size - fromState.zip(toState).takeWhile { it.first == it.second }.count()
    }
}
