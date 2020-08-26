package com.justai.jaicf.activator.lex

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import software.amazon.awssdk.services.lexruntime.model.DialogState.*

class LexIntentActivator(
    model: ScenarioModel,
    private val connector: LexConnector
) : BaseIntentActivator(model) {

    override val name = "lexIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val lexResponse = connector.postText(request.clientId, request.input)

        return when (lexResponse.dialogState()) {
            FULFILLED, READY_FOR_FULFILLMENT, ELICIT_SLOT -> {
                val intent = lexResponse.intentName()
                val confidence = lexResponse.nluIntentConfidence().score().toFloat()
                IntentActivatorContext(confidence, intent)
            }
            else -> null
        }
    }

    class Factory(private val connector: LexConnector) : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return LexIntentActivator(model, connector)
        }
    }
}