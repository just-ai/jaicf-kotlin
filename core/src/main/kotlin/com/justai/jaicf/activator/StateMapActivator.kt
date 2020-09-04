package com.justai.jaicf.activator

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition

/**
 * A helper abstraction for every [Activator] that should activate a state if it contains a particular rule like event or intent.
 *
 * @see com.justai.jaicf.activator.event.BaseEventActivator
 * @see com.justai.jaicf.activator.intent.BaseIntentActivator
 */
abstract class StateMapActivator(model: ScenarioModel): Activator {

    private val transitions = model.transitions.filter { canHandleRule(it.rule) }.groupBy { it.fromState }

    protected abstract fun canHandleRule(rule: ActivationRule): Boolean

    protected abstract fun getRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher?

    override fun activate(botContext: BotContext, request: BotRequest): Activation? {
        val matcher = getRuleMatcher(botContext, request) ?: return null
        val transitions = generateTransitions(botContext)

        return transitions.mapNotNull { transition ->
            matcher.match(transition.rule)?.let { Activation(transition.toState, it) }
        }.firstOrNull()
    }

    private fun generateTransitions(botContext: BotContext): Sequence<Transition> {
        val currentState = StatePath.parse(botContext.dialogContext.currentContext).resolve(".")
        val states = generateSequence(currentState) { if (it.toString() == "/") null else it.stepUp() }
        return states.flatMap {
            transitions[it.toString()]?.asSequence() ?: emptySequence()
        }
    }
}