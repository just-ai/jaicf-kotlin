package com.justai.jaicf.context

import com.justai.jaicf.model.scenario.ScenarioModel
import java.io.Serializable
import java.util.ArrayDeque

/**
 * Contains all data regarding the current state of the dialogue.
 * Please be careful and edit this class variables values only if you clearly understand what you do.
 */
class DialogContext: Serializable {

    var nextContext: String? = null
    var currentContext: String = "/"
    var nextState: String? = null
    var currentState: String = "/"

    val transitions: MutableMap<String, String> = mutableMapOf()
    val backStateStack = ArrayDeque<String>()
    var transitionHistory: ArrayDeque<String> = ArrayDeque<String>(TRANSITION_HISTORY_SIZE_LIMIT)
        get() {
            // don't remove, may be null due to java serialization
            field = field ?: ArrayDeque<String>(TRANSITION_HISTORY_SIZE_LIMIT)
            return field.apply { if (isEmpty()) add(currentState) }
        }
        private set

    fun nextState(): String? {
        nextState?.let {
            saveToTransitionHistory(it)
            currentState = it
            nextState = null
            return currentState
        }
        return null
    }

    fun nextContext(model: ScenarioModel): String {
        model.states[currentState]?.let {
            if (!it.noContext) {
                currentContext = it.path.toString()
            }
        }
        nextContext?.let {
            currentContext = it
            nextContext = null
        }

        return currentContext
    }

    fun saveToTransitionHistory(state: String) {
        while (transitionHistory.size >= TRANSITION_HISTORY_SIZE_LIMIT) {
            transitionHistory.removeFirst()
        }
        transitionHistory.add(state)
    }

    private fun createTransitionHistory() = ArrayDeque<String>(TRANSITION_HISTORY_SIZE_LIMIT).apply { add(currentState) }

    companion object {
        private const val serialVersionUID = -9180292787182200322L
        private const val TRANSITION_HISTORY_SIZE_LIMIT: Int = 50
    }
}
