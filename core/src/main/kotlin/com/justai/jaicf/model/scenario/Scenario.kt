package com.justai.jaicf.model.scenario

import com.justai.jaicf.builder.Scenario
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Scenario : ReadOnlyProperty<Any, ScenarioModel> {
    val model: ScenarioModel
    override fun getValue(thisRef: Any, property: KProperty<*>) = model
}

fun createScenario(model: () -> ScenarioModel): Scenario = object : Scenario {
    override val model by lazy(model)
}