package com.justai.jaicf.builder

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.model.ScenarioModelBuilder
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.model.scenario.createScenario
import com.justai.jaicf.reactions.Reactions

@ScenarioDsl
fun <B : BotRequest, R : Reactions> Scenario(
    channelToken: ChannelTypeToken<B, R>,
    body: ScenarioBuilder<B, R>.() -> Unit
): Scenario = createScenario { ScenarioBuilder(channelToken).apply(body).build() }

@ScenarioDsl
fun Scenario(
    body: ScenarioBuilder<BotRequest, Reactions>.() -> Unit
): Scenario = Scenario(ChannelTypeToken.Default, body)

@ScenarioDsl
fun <B : BotRequest, R : Reactions> startScenario(
    channelToken: ChannelTypeToken<B, R>,
    body: RootBuilder<B, R>.() -> Unit
): Scenario = Scenario(channelToken) { start(body) }

@ScenarioDsl
fun startScenario(
    body: RootBuilder<BotRequest, Reactions>.() -> Unit
): Scenario = Scenario(ChannelTypeToken.Default) { start(body) }

infix fun Scenario.append(other: Scenario): Scenario = createScenario {
    ScenarioModelBuilder().also {
        it.dependencies += listOf(scenario, other.scenario)
    }.build()
}
