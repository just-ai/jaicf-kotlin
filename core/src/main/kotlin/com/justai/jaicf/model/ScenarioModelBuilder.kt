package com.justai.jaicf.model

import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule
import com.justai.jaicf.activator.intent.IntentByNameActivationRule
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookListener
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition

class ScenarioModelBuilder {

    val states: MutableList<State>
    val transitions: MutableList<Transition>
    val hooks: MutableList<BotHookListener<BotHook>>

    constructor() {
        states = mutableListOf()
        transitions = mutableListOf()
        hooks = mutableListOf()
    }

    private constructor(other: ScenarioModelBuilder) {
        states = other.states
        transitions = other.transitions
        hooks = other.hooks
    }

    fun append(path: StatePath, other: Scenario, ignoreHooks: Boolean, exposeHooks: Boolean, propagateHooks: Boolean) {
        val otherModel = other.model.resolve(path)
        states += otherModel.states.values.filterNot { it.path.toString() == path.toString() }
        transitions += otherModel.transitions

        if (!propagateHooks) {
            hooks.replaceAll { hook ->
                if (path.toString().startsWith(hook.availableFrom.toString())) {
                    hook.copy(exceptFrom = hook.exceptFrom + path)
                } else {
                    hook
                }
            }
        }

        if (!ignoreHooks) {
            hooks += if (exposeHooks) {
                otherModel.hooks.map { hook -> hook.copy(availableFrom = StatePath.root()) }
            } else {
                otherModel.hooks
            }
        }
    }

    fun build(): ScenarioModel = verify().postProcess()

    private fun verify(): ScenarioModelBuilder {
        val paths = mutableSetOf<String>()
        states.map { it.path.toString() }.forEach {
            check(paths.add(it)) { "Duplicated declaration of state with path '$it'." }
        }
        return this
    }

    private fun postProcess(): ScenarioModel {
        transitions.groupBy { it.fromState }.forEach { (_, transitions) ->
            val intents = transitions.map { it.rule }.filterIsInstance<IntentByNameActivationRule>().map { it.intent }
            val events = transitions.map { it.rule }.filterIsInstance<EventByNameActivationRule>().map { it.event }

            transitions.forEach {
                when (it.rule) {
                    is AnyIntentActivationRule -> it.rule.except.apply {
                        clear()
                        addAll(intents)
                    }

                    is AnyEventActivationRule -> it.rule.except.apply {
                        clear()
                        addAll(events)
                    }
                }
            }
        }

        return ScenarioModel(states.associateBy { it.path.toString() }, transitions, hooks)
    }

    private fun ScenarioModel.resolve(statePath: StatePath): ScenarioModel {
        fun StatePath.resolveRelatively(other: String): StatePath = resolve(other.substring(1))
        fun StatePath.resolveRelatively(other: StatePath): StatePath = resolveRelatively(other.toString())

        val newStates = states.values.map {
            it.copy(path = statePath.resolveRelatively(it.path))
        }
        val newTransitions = transitions.map {
            it.copy(
                fromState = statePath.resolveRelatively(it.fromState).toString(),
                toState = statePath.resolveRelatively(it.toState).toString()
            )
        }
        val newHooks = hooks.map { hook ->
            hook.copy(
                availableFrom = statePath.resolveRelatively(hook.availableFrom),
                exceptFrom = hook.exceptFrom.map { statePath.resolveRelatively(it) }.toSet(),
            )
        }
        return ScenarioModel(newStates.associateBy { it.path.toString() }, newTransitions, newHooks)
    }
}
