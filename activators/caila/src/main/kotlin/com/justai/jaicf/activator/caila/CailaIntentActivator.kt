package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.caila.client.CailaHttpClient
import com.justai.jaicf.activator.caila.client.CailaKtorClient
import com.justai.jaicf.activator.caila.slotfilling.CailaSlotfillingHelper
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFiller
import com.justai.jaicf.slotfilling.SlotFillingResult


class CailaIntentActivator(
    model: ScenarioModel,
    private val settings: CailaNLUSettings,
    private val client: CailaHttpClient = CailaKtorClient(settings.accessToken, settings.cailaUrl, settings.classifierNBest)
) : BaseIntentActivator(model) {

    override val name = "cailaIntentActivator"

    private val slotFillingHelper = CailaSlotfillingHelper(client)

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun fillSlots(
        botContext: BotContext,
        request: BotRequest,
        reactions: Reactions,
        activatorContext: ActivatorContext?,
        slotFiller: SlotFiller?
    ): SlotFillingResult = slotFillingHelper.process(botContext, request, reactions, activatorContext, slotFiller)

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val results = client.analyze(request.input) ?: return null

        val intent = results.inference.variants
            .filter { it.confidence >= settings.confidenceThreshold }
            .map { findState(it.intent.name, botContext) to it }
            .maxBy { it.first?.count { c -> c == '/' } ?: 0 }?.second

        return when {
            intent != null -> return CailaIntentActivatorContext(results, intent)
            else -> null
        }
    }

    class Factory(private val settings: CailaNLUSettings) : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return CailaIntentActivator(model, settings)
        }
    }
}
