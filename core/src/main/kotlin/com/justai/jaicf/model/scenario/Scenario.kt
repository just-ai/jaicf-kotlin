package com.justai.jaicf.model.scenario

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.builder.ScenarioBuilder
import kotlin.reflect.KProperty

interface Scenario {
    val scenario: ScenarioModel
}

operator fun Scenario.getValue(thisRef: Scenario, property: KProperty<*>): ScenarioModel = scenario

fun createScenario(model: () -> ScenarioModel): Scenario = object : Scenario {
    override val scenario by lazy(model)
}
