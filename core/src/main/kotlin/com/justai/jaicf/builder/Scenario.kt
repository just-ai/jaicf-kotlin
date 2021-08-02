package com.justai.jaicf.builder

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.model.ScenarioModelBuilder
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.plugin.StateBody
import com.justai.jaicf.plugin.StateDeclaration
import com.justai.jaicf.reactions.Reactions


@ScenarioDsl
@StateDeclaration("")
fun Scenario(
    @StateBody body: RootBuilder<BotRequest, Reactions>.() -> Unit
): Scenario = Scenario(ChannelTypeToken.Default, body)

@ScenarioDsl
@StateDeclaration("")
fun <B : BotRequest, R : Reactions> Scenario(
    channelToken: ChannelTypeToken<B, R>,
    @StateBody body: RootBuilder<B, R>.() -> Unit,
): Scenario = object : Scenario {
    override val model by lazy { createModel(channelToken, body) }
}

@ScenarioDsl
@StateDeclaration("")
fun createModel(
    @StateBody body: RootBuilder<BotRequest, Reactions>.() -> Unit
): ScenarioModel = createModel(ChannelTypeToken.Default, body)

@ScenarioDsl
@StateDeclaration("")
fun <B : BotRequest, R : Reactions> createModel(
    channelToken: ChannelTypeToken<B, R>,
    @StateBody body: RootBuilder<B, R>.() -> Unit,
): ScenarioModel = RootBuilder(ScenarioModelBuilder(), channelToken).apply(body).buildScenario()

infix fun Scenario.append(other: Scenario): Scenario = object : Scenario {
    override val model by lazy {
        ScenarioModelBuilder().also {
            it.states += State(StatePath.root(), noContext = false, modal = false)
            it.append(StatePath.root(), this@append, ignoreHooks = false, exposeHooks = true, propagateHooks = true)
            it.append(StatePath.root(), other, ignoreHooks = false, exposeHooks = true, propagateHooks = true)
        }.build()
    }
}
