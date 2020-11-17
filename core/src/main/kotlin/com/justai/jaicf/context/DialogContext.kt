package com.justai.jaicf.context

import com.justai.jaicf.model.scenario.ScenarioModel
import java.io.Serializable
import java.util.ArrayDeque

/**
 * Contains all data regarding the current state of the dialogue.
 * Please be careful and edit this class variables values only if you clearly understand what you do.
 */
data class DialogContext(
    var nextContext: String? = null,
    var currentContext: String = "/",
    var nextState: String? = null,
    var currentState: String = "/",

    val transitions: MutableMap<String, String> = mutableMapOf(),
    val backStateStack: ArrayDeque<String> = ArrayDeque()
) : Serializable {

    fun nextState(): String? {
        currentState = nextState ?: return null
        nextState = null
        return currentState
    }

    fun nextContext(model: ScenarioModel): String? {
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
}