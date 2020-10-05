package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.caila.client.CailaHttpClient
import com.justai.jaicf.activator.caila.client.CailaKtorClient
import com.justai.jaicf.activator.caila.slotfilling.CailaSlotFillingHelper
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFillingResult
import com.justai.jaicf.slotfilling.SlotReactor


class CailaIntentActivator(
    model: ScenarioModel,
    private val settings: CailaNLUSettings,
    private val client: CailaHttpClient = CailaKtorClient(
        settings.accessToken,
        settings.cailaUrl,
        settings.classifierNBest
    )
) : BaseIntentActivator(model) {

    override val name = "cailaIntentActivator"

    private val slotFillingHelper = CailaSlotFillingHelper(client, settings.cailaSlotFillingSettings)

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun fillSlots(
        request: BotRequest,
        reactions: Reactions,
        botContext: BotContext,
        activatorContext: ActivatorContext?,
        slotReactor: SlotReactor?
    ): SlotFillingResult =
        slotFillingHelper.process(botContext, request, reactions, activatorContext, slotReactor)

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val results = client.analyze(request.input) ?: return emptyList()
        return results.inference.variants.filter {
            it.confidence >= settings.confidenceThreshold
        }.map { CailaIntentActivatorContext(results, it) }
    }

    class Factory(private val settings: CailaNLUSettings) : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return CailaIntentActivator(model, settings)
        }
    }
}
