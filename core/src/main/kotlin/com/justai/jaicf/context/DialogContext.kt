package com.justai.jaicf.context

import com.justai.jaicf.model.scenario.ScenarioModel
import com.sun.xml.internal.bind.v2.TODO
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.util.ArrayDeque
import java.util.Collections.addAll

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
    var transitionHistory = ArrayDeque<String>(TRANSITION_HISTORY_SIZE_LIMIT).apply { add(currentState) }
        private set

    fun nextState(): String? {
        currentState = nextState ?: return null
        nextState = null
        return currentState.also(::saveToTransitionHistory)
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
        ensureDeserialized()
        while (transitionHistory.size >= TRANSITION_HISTORY_SIZE_LIMIT) {
            transitionHistory.removeFirst()
        }
        transitionHistory.add(state)
    }

    private fun ensureDeserialized() {
        // may be null after java deserialization
        @Suppress("SENSELESS_COMPARISON")
        if (transitionHistory == null) {
            transitionHistory = ArrayDeque<String>(TRANSITION_HISTORY_SIZE_LIMIT).apply { add(currentState) }
        }
    }

    companion object {
        private const val serialVersionUID = -9180292787182200322L
        private const val TRANSITION_HISTORY_SIZE_LIMIT: Int = 50
    }
}
