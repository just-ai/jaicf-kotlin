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
import com.justai.jaicf.model.state.StatePath
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

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext? {
        val results = client.analyze(request.input) ?: return null

        val transitionsPerContextSorted = results.inference.variants
            .filter { it.confidence >= settings.confidenceThreshold }
            .mapNotNull {
                findState(it.intent.name, botContext)?.let { state ->
                    state to it
                }
            }
            // Sort all predicted intents by context relevance
            .sortedByDescending {
                var currentState = botContext.dialogContext.currentState
                if (currentState == it.first) {
                    currentState = StatePath.parse(currentState).parent
                }
                currentState.commonPrefixWith(it.first)
            }
            .groupBy { it.first }

        // From most relevant by context take intent with maximum confidence.
        val intent = transitionsPerContextSorted.values
            .firstOrNull()
            ?.maxBy { it.second.confidence }?.second

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
