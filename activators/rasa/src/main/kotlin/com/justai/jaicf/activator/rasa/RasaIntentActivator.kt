package com.justai.jaicf.activator.rasa

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.rasa.api.RasaApi
import com.justai.jaicf.activator.rasa.api.RasaParseMessageRequest
import com.justai.jaicf.activator.rasa.api.RasaParseMessageResponse
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import kotlinx.serialization.json.jsonObject
import java.util.*

class RasaIntentActivator(
    model: ScenarioModel,
    private val api: RasaApi,
    private val confidenceThreshold: Double
) : BaseIntentActivator(model) {

    override val name = "rasaIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val messageId = UUID.randomUUID().toString()
        val rawJson = api.parseMessage(RasaParseMessageRequest(request.input, messageId)) ?: return emptyList()
        val json = api.Json.parseToJsonElement(rawJson).jsonObject
        val response = api.Json.decodeFromJsonElement(RasaParseMessageResponse.serializer(), json)

        response.ranking ?: return emptyList()

        return response.ranking
            .filter { it.confidence > confidenceThreshold }
            .map { RasaActivatorContext(it, response.entities.orEmpty(), json) }
    }

    class Factory(
        private val api: RasaApi,
        private val confidenceThreshold: Double = 0.0
    ) : ActivatorFactory {
        override fun create(model: ScenarioModel) = RasaIntentActivator(model, api, confidenceThreshold)
    }
}