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
): Scenario = createScenario { ScenarioBuilder(channelToken).apply(body).buildScenario() }

@ScenarioDsl
fun Scenario(
    body: ScenarioBuilder<BotRequest, Reactions>.() -> Unit
): Scenario = Scenario(ChannelTypeToken.Default, body)

infix fun Scenario.append(other: Scenario): Scenario = createScenario {
    ScenarioModelBuilder().also {
        it.dependencies += listOf(scenario, other.scenario)
    }.build()
}
