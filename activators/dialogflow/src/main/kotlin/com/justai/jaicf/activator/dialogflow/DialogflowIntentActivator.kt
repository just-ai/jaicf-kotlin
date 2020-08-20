package com.justai.jaicf.activator.dialogflow

import com.google.cloud.dialogflow.v2.QueryParameters
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasEvent
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class DialogflowIntentActivator(
    model: ScenarioModel,
    private val connector: DialogflowConnector
): BaseIntentActivator(model) {

    override val name = "dialogflowIntentActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery() || request.hasEvent()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): DialogflowActivatorContext? {
//        val params = QueryParameters.newBuilder().addSessionEntityTypes(
//            SessionEntityType.newBuilder().setName("")
//                .addEntities(
//                    EntityType.Entity.newBuilder().setValue("").addAllSynonyms(mutableListOf())
//                )
//        ).build()

        //TODO query parameters and session entities
        val params = QueryParameters.getDefaultInstance()

        val qr = when {
            request.hasQuery() -> connector.detectIntentByQuery(request, params)
            else -> connector.detectIntentByEvent(request, params)
        }

        val intent = when {
            qr.intent.displayName.isNotEmpty() -> qr.intent.displayName
            qr.action.startsWith(DialogflowIntent.SMALLTALK) -> DialogflowIntent.SMALLTALK
            else -> null
        }

        return intent?.let { DialogflowActivatorContext(intent, qr) }
    }

    class Factory(private val connector: DialogflowConnector): ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return DialogflowIntentActivator(model, connector)
        }
    }

}