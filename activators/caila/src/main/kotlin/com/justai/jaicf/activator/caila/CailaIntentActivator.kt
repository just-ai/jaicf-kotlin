package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.caila.connector.CailaConnector
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel


class CailaIntentActivator(
    model: ScenarioModel,
    private val settings: CailaNLUSettings
) : BaseIntentActivator(model) {

    private val connector = CailaConnector(settings.projectId, settings.cailaUrl)

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val results = connector.simpleInference(request.input) ?: return null

        return when {
            results.confidence > settings.confidenceThreshold -> return CailaIntentActivatorContext(results)
            else -> null
        }
    }

    class Factory(private val settings: CailaNLUSettings) : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return CailaIntentActivator(model, settings)
        }
    }
}
