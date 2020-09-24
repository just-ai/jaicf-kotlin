package com.justai.jaicf.model.activation

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.state.isIndirectChildOf

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

/**
 * Sorts list of activation by relevance to currentContext - first child nodes, then siblings, then parents, etc.
 * */
fun Collection<Activation>.sortedByContext(currentContext: String): List<Activation> {
    val (fromRoot, onPath) = partition {
        val isRoot = it.state!!.commonPrefixWith(currentContext) == "/"
        val isIndirectChild = it.state.asStatePath().isIndirectChildOf(currentContext.asStatePath())
        isRoot || isIndirectChild
    }
    return onPath.sortedByDescending { it.state!!.asStatePath().components.size } + fromRoot.sortedBy { it.state!!.asStatePath().components.size }
}

/**
 * Calculates context difference (number of transitions) between targetState and currentState.
 * e.g.: currentState = "/root/child1/child2/child3", targetState = "/root/child1"; diff level = 2
 *
 * Transition into child costs 0
 * Transition into same state == transition to sibling, costs 1
 *
 * @param targetState possible target state by activation
 * @param currentContext current context
 *
 * @return number of transitions between current and target state
 * */
fun ActivationSelector.calculateStatesDifference(targetState: StatePath, currentContext: StatePath): Int {
    if (targetState.parent == currentContext.toString()) return 0
    if (currentContext.toString() == targetState.toString()) return 1

    val toState = targetState.components
    val fromState = currentContext.components

    return when (targetState.isIndirectChildOf(currentContext)) {
        true -> toState.size - fromState.size
        false -> fromState.size - fromState.zip(toState).takeWhile { it.first == it.second }.count()
    }
}

private fun String.asStatePath() = StatePath.parse(this)