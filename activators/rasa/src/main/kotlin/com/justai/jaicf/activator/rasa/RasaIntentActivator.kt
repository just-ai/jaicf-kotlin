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
import java.util.UUID

class RasaIntentActivator(
    model: ScenarioModel,
    private val api: RasaApi,
    private val confidenceThreshold: Double
): BaseIntentActivator(model) {

    override val name = "rasaIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val messageId = UUID.randomUUID().toString()
        val response = api.parseMessage(RasaParseMessageRequest(request.input, messageId)) ?: return emptyList()

        return response.ranking
                .filter { it.confidence > confidenceThreshold }
                .map { IntentActivatorContext(it.confidence, it.name) }
    }

    class Factory(
        private val api: RasaApi,
        private val confidenceThreshold: Double = 0.0
    ): ActivatorFactory {
        override fun create(model: ScenarioModel) = RasaIntentActivator(model, api, confidenceThreshold)
    }
}