package com.justai.jaicf.model.scenario

import kotlin.reflect.KProperty

interface Scenario {
    val scenario: ScenarioModel
}

operator fun Scenario.getValue(thisRef: Scenario, property: KProperty<*>): ScenarioModel = scenario
