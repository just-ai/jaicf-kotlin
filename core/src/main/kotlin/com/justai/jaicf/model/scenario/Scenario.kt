package com.justai.jaicf.model.scenario

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.RootBuilder
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.model.ScenarioModelBuilder
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KProperty

interface Scenario {
    val scenario: ScenarioModel

    @ScenarioDsl
    fun createScenario(
        body: RootBuilder<BotRequest, Reactions>.() -> Unit
    ): ScenarioModel = com.justai.jaicf.builder.Scenario(ChannelTypeToken.Default, body).scenario

    @ScenarioDsl
    fun <B : BotRequest, R : Reactions> createScenario(
        channelToken: ChannelTypeToken<B, R>,
        body: RootBuilder<B, R>.() -> Unit
    ): ScenarioModel = object : Scenario {
        override val scenario by lazy { RootBuilder(ScenarioModelBuilder(), channelToken).apply(body).buildScenario() }
    }.scenario
}

operator fun Scenario.getValue(thisRef: Scenario, property: KProperty<*>): ScenarioModel = scenario
