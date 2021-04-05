package com.justai.jaicf.model.scenario

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.RootBuilder
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KProperty

interface Scenario {
    val scenario: ScenarioModel

    @ScenarioDsl
    fun Scenario.createScenario(
        body: RootBuilder<BotRequest, Reactions>.() -> Unit
    ): ScenarioModel = Scenario(ChannelTypeToken.Default, body).scenario

    @ScenarioDsl
    fun <B : BotRequest, R : Reactions> Scenario.createScenario(
        channelToken: ChannelTypeToken<B, R>,
        body: RootBuilder<B, R>.() -> Unit
    ): ScenarioModel = Scenario(channelToken, body).scenario
}

operator fun Scenario.getValue(thisRef: Scenario, property: KProperty<*>): ScenarioModel = scenario
