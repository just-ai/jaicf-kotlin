package com.justai.jaicf.channel.yandexalice.activator

import com.justai.jaicf.activator.Activator
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

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val aliceRequest = request as? AliceBotRequest ?: return null

        val intent = aliceRequest.request?.let { req ->
            req.nlu.intents.map { entry ->
                findState(entry.key, botContext) to entry
            }.maxBy { it.first?.count { c -> c == '/'} ?: 0 }?.second
        }

        return intent?.let {
            AliceIntentActivatorContext(it.key, it.value)
        }
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = AliceIntentActivator(model)
    }
}