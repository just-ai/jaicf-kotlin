package com.justai.jaicf.model.scenario

import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookAction
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.transition.Transition
import kotlin.reflect.KClass

/**
 * Represents a model of the dialog scenario.
 * Contains a required data that is used by [com.justai.jaicf.api.BotApi] implementation during the user's request processing.
 * This class should not be used directly from your code. You should use [com.justai.jaicf.builder.ScenarioBuilder] instead.
 * Can be concatenated with other models to create a merged [ScenarioModel].
 *
 * @see com.justai.jaicf.builder.ScenarioBuilder
 */
class ScenarioModel {
    val transitions = mutableListOf<Transition>()
    val states = mutableMapOf<String, State>()
    val hooks = mutableMapOf<KClass<out BotHook>, MutableList<BotHookAction<in BotHook>>>()

    operator fun plus(other: ScenarioModel): ScenarioModel {
        val model = ScenarioModel()

        model.transitions.addAll(transitions + other.transitions)
        model.states.putAll(states + other.states)
        model.hooks.putAll(hooks)
        other.hooks.forEach { (event, listeners) ->
            model.hooks.putIfAbsent(event, mutableListOf())
            model.hooks[event]?.addAll(listeners)
        }

        return model
    }
}