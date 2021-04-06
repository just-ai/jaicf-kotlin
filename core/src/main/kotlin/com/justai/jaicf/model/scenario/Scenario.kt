package com.justai.jaicf.model.scenario

import kotlin.reflect.KProperty

/**
 * Main interface for Scenario objects.
 *
 * Scenario objects should be generally created by using [com.justai.jaicf.builder.Scenario] function.
 * Alternative way for creating a scenario is by implementing this interface, see example:
 * ```kotlin
 * class MyScenario() : Scenario {
 *   override val model: ScenarioModel = createModel {
 *     // your states
 *     }
 * }
 *
 * ```
 *
 * @property model model of scenario
 *
 * @see [com.justai.jaicf.builder.Scenario]
 * @see [com.justai.jaicf.builder.createModel]
 * */
interface Scenario {
    val model: ScenarioModel
}

operator fun Scenario.getValue(thisRef: Scenario, property: KProperty<*>): ScenarioModel = model
