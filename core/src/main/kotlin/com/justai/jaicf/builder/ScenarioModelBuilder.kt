package com.justai.jaicf.builder

import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule
import com.justai.jaicf.activator.intent.IntentByNameActivationRule
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookAction
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition
import kotlin.reflect.KClass

internal class ScenarioModelBuilder {

    val dependencies: MutableList<ScenarioModel>
    val states: MutableList<State>
    val transitions: MutableList<Transition>
    val hooks: MutableMap<KClass<out BotHook>, MutableList<BotHookAction<in BotHook>>>

    constructor() {
        dependencies = mutableListOf()
        states = mutableListOf()
        transitions = mutableListOf()
        hooks = mutableMapOf()
    }

    private constructor(other: ScenarioModelBuilder) {
        dependencies = mutableListOf()
        states = other.states
        transitions = other.transitions
        hooks = other.hooks
    }

    fun build(): ScenarioModel = concatenate().verify().postProcess()

    private fun concatenate(): ScenarioModelBuilder {
        return dependencies.fold(ScenarioModelBuilder(this)) { builder, model ->
            builder.apply {
                states.addAll(model.states.values.filterNot { it.path.isRoot })
                transitions.addAll(model.transitions)
                model.hooks.entries.forEach { (klass, listeners) ->
                    builder.hooks.computeIfAbsent(klass) { mutableListOf() }.addAll(listeners)
                }
            }
        }
    }

    private fun verify(): ScenarioModelBuilder {
        val paths = mutableSetOf<String>()
        states.map { it.path.toString() }.forEach {
            check(paths.add(it)) { "Duplicated declaration of state with path '$it'." }
        }
        transitions.forEach {
            val missing = it.fromState.takeIf { it !in paths } ?: it.toState.takeIf { it !in paths }
            check(missing == null) {
                "Cannot create transition from '${it.fromState}' to '${it.toState}'" +
                        " as the state with path '${it.fromState}' doesn't exist."
            }
        }
        return this
    }

    private fun postProcess(): ScenarioModel {
        val newTransitions = transitions.groupBy { it.fromState }.flatMap { (_, transitions) ->
            val intents = transitions.map { it.rule }.filterIsInstance<IntentByNameActivationRule>().map { it.intent }
            val events = transitions.map { it.rule }.filterIsInstance<EventByNameActivationRule>().map { it.event }

            transitions.map {
                when (it.rule) {
                    is AnyIntentActivationRule -> it.copy(rule = AnyIntentActivationRule(intents))
                    is AnyEventActivationRule -> it.copy(rule = AnyEventActivationRule(events))
                    else -> it
                }
            }
        }

        return ScenarioModel(states.associateBy { it.path.toString() }, newTransitions, hooks)
    }
}