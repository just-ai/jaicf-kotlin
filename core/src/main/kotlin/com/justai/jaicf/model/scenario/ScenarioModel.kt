package com.justai.jaicf.model.scenario

import com.justai.jaicf.activator.selection.isTo
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookListener
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition

/**
 * Represents a model of the dialog scenario.
 * Contains a required data that is used by [com.justai.jaicf.api.BotApi] implementation during the user's request processing.
 * This class should not be used directly from your code. You should use [com.justai.jaicf.builder.Scenario] or [com.justai.jaicf.builder.createModel] instead.
 * Can be concatenated with other models to create a merged [ScenarioModel].
 *
 * @see com.justai.jaicf.builder.Scenario
 * @see com.justai.jaicf.builder.createModel
 */
data class ScenarioModel(
    val states: Map<String, State> = mapOf(),
    val transitions: List<Transition> = listOf(),
    val hooks: List<BotHookListener<BotHook>> = listOf()
) {

    fun verify(): ScenarioModel {
        val paths = states.keys
        transitions.forEach {
            val missing = it.fromState.takeIf { it !in paths } ?: it.toState.takeIf { it !in paths }
            check(missing == null) {
                "Cannot create transition from '${it.fromState}' to '${it.toState}'" +
                        " as the state with path '${it.fromState}' doesn't exist."
            }
        }
        return this
    }

    fun generateTransitions(botContext: BotContext): List<Transition> {
        val currentPath = StatePath.parse(botContext.dialogContext.currentContext)

        val allStatesBack = listOf(currentPath.toString()) + currentPath.parents.reversedArray()

        val transitionsFrom = transitions.groupBy { it.fromState }
        val availableTransitions = mutableListOf<Transition>()

        for (state in allStatesBack) {
            availableTransitions += transitionsFrom[state] ?: emptyList()

            if (states[state]?.modal == true) {
                val parent = StatePath.parse(state).parent
                availableTransitions += transitionsFrom[parent]?.filter { it.isTo(state) } ?: emptyList()
                break
            }
        }

        return availableTransitions
    }
}