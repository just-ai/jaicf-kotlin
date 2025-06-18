package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.catchall.CatchAllActivationRule
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.catchall.CatchAllActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.tempProperty

import com.justai.jaicf.model.scenario.ScenarioModel

class CailaCatchAllActivator(model: ScenarioModel) : CatchAllActivator(model) {

    override val name = "cailaCatchAllActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest) =
        ruleMatcher<CatchAllActivationRule> {
            botContext.cailaAnalyzeResult?.let { result ->
                CailaCatchAllActivatorContext(result)
            } ?: CatchAllActivatorContext()
        }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = CailaCatchAllActivator(model)
    }
}

var BotContext.cailaAnalyzeResult by tempProperty<CailaAnalyzeResponseData?>(removeOnNull = true) { null }
