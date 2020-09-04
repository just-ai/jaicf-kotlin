package com.justai.jaicf.channel.yandexalice.activator

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class AliceIntentActivator(model: ScenarioModel): BaseIntentActivator(model) {

    override val name = "aliceIntentActivator"

    override fun canHandle(request: BotRequest) = request is AliceBotRequest

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val aliceRequest = request as? AliceBotRequest ?: return emptyList()
        return aliceRequest.request?.nlu?.intents?.map {
            AliceIntentActivatorContext(it.key, it.value)
        } ?: emptyList()
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = AliceIntentActivator(model)
    }
}