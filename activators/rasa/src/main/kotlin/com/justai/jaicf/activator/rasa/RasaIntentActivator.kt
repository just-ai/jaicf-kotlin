package com.justai.jaicf.activator.rasa

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.rasa.api.RasaApi
import com.justai.jaicf.activator.rasa.api.RasaParseMessageRequest
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class RasaIntentActivator(
    model: ScenarioModel,
    private val api: RasaApi,
    private val confidenceThreshold: Double
): BaseIntentActivator(model) {

    override val name = "rasaIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val response = api.parseMessage(RasaParseMessageRequest(request.input)) ?: return emptyList()

        return when {
            response.intent.confidence > confidenceThreshold -> listOf(RasaActivatorContext(response))
            else -> emptyList()
        }
    }

    class Factory(
        private val api: RasaApi,
        private val confidenceThreshold: Double = 0.0
    ): ActivatorFactory {
        override fun create(model: ScenarioModel) = RasaIntentActivator(model, api, confidenceThreshold)
    }
}