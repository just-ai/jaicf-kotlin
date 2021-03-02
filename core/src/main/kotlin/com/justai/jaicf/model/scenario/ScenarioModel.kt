package com.justai.jaicf.model.scenario

import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookListener
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
data class ScenarioModel(
    val states: Map<String, State> = mapOf(),
    val transitions: List<Transition> = listOf(),
    val hooks: List<BotHookListener<BotHook>> = listOf()
)