package com.justai.jaicf.activator.caila.slotfilling


import com.justai.jaicf.activator.caila.CailaIntentActivatorContext
import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.activator.caila.client.CailaHttpClient
import com.justai.jaicf.activator.caila.dto.CailaEntityMarkupData
import com.justai.jaicf.activator.caila.dto.CailaKnownSlotData
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFillingFinished
import com.justai.jaicf.slotfilling.SlotFillingInProgress
import com.justai.jaicf.slotfilling.SlotFillingInterrupted
import com.justai.jaicf.slotfilling.SlotFillingResult
import com.justai.jaicf.slotfilling.SlotReactor


internal class CailaSlotFillingHelper(
    private val cailaClient: CailaHttpClient,
    private val cailaSlotFillingSettings: CailaSlotFillingSettings = CailaSlotFillingSettings.DEFAULT
) {

    fun process(
        botContext: BotContext,
        botRequest: BotRequest,
        reactions: Reactions,
        initialActivationContext: ActivatorContext?,
        slotReactor: SlotReactor?
    ): SlotFillingResult {
        if (initialActivationContext != null && initialActivationContext !is CailaIntentActivatorContext) {
            return SlotFillingFinished(initialActivationContext)
        }

        if (initialActivationContext == null && botRequest.input.matches(Regex("/start", RegexOption.IGNORE_CASE))) {
            return SlotFillingInterrupted()
        }

        val ctx = restoreContext(botContext, initialActivationContext)
        val required = ctx.slots.filter { it.required }
        val known = ctx.knownSlots
        if (known.map { it.name }.containsAll(required.map { it.name })) {
            return SlotFillingFinished(ctx.initialActivatorContext)
        }

        val actionContext = ActionContext(botContext, ctx.initialActivatorContext, botRequest, reactions)
        val filled = fillSlots(ctx, botRequest.input)

        for (slot in required) {
            if (slot.required && slot.name !in filled) {
                if (checkRetriesInterrupts(ctx, slot.name) || checkIntentInterrupts(ctx, botRequest.input)) {
                    clearSlotFillingContext(botContext)
                    return SlotFillingInterrupted()
                }

                if (slotReactor?.canReact(slot.name) == true) {
                    slotReactor.react(
                        botRequest,
                        botContext,
                        reactions,
                        ctx.initialActivatorContext,
                        slot.name,
                        slot.prompts ?: emptyList()
                    )
                } else {
                    with(actionContext) {
                        slot.prompts?.let { prompts ->
                            if (prompts.isEmpty()) return SlotFillingFinished(ctx.initialActivatorContext)
                            reactions.sayRandom(*prompts.toTypedArray())
                        } ?: return SlotFillingFinished(ctx.initialActivatorContext)
                    }
                }
                saveSlotFillingContext(botContext, ctx)
                return SlotFillingInProgress
            }
        }

        val newSlots = ctx.knownSlots.map { it.name to it.value }.toMap()
        val newEntities = ctx.knownEntities
        ctx.initialActivatorContext.caila?.slots = newSlots
        ctx.initialActivatorContext.caila?.result?.entitiesLookup?.entities = newEntities
        clearSlotFillingContext(botContext)
        return SlotFillingFinished(ctx.initialActivatorContext)
    }

    private fun fillSlots(ctx: CailaSlotFillingContext, text: String): List<String> {
        val md = cailaClient.entitiesLookup(text) ?: error("Failed to query CAILA for entities")
        val default = md.entities.filter { it.default == true }
        val other = md.entities
            .filter { it.default != true }
            .filter { notDefault ->
                val defaultEntity = default.find { it.entity == notDefault.entity } ?: return@filter true
                val isDefaultContainsText = defaultEntity.text.contains(notDefault.text)
                val isDefaultLonger = defaultEntity.text > notDefault.text
                isDefaultContainsText.not() && isDefaultLonger.not()
            }
        // first fill default entities, then other if it's needed
        (default + other).map { entity -> tryFillSlot(ctx, entity) }
        return ctx.filledSlots
    }

    private fun tryFillSlot(ctx: CailaSlotFillingContext, e: CailaEntityMarkupData) {
        for (s in ctx.slotForEntity(e)) {
            val valueAtPos = "${e.value}-${e.startPos}"
            val isArray = s.array ?: false
            if (isArray || (!ctx.filledSlots.contains(s.name) && !ctx.filledValues.contains(valueAtPos))) {
                e.slot = s.name
                ctx.knownSlots.add(CailaKnownSlotData(s.name, e.value, isArray))
                ctx.knownEntities.add(e)
            }
        }
    }

    private fun checkRetriesInterrupts(ctx: CailaSlotFillingContext, name: String): Boolean {
        val curr = ctx.maxRetries[name]!!.inc()
        ctx.maxRetries[name] = curr
        return curr > cailaSlotFillingSettings.maxSlotRetries
    }

    private fun checkIntentInterrupts(ctx: CailaSlotFillingContext, text: String?): Boolean {
        if (!cailaSlotFillingSettings.stopOnAnyIntent || text == null) {
            return false
        }

        val inference = cailaClient.simpleInference(text) ?: return false
        if (inference.confidence > cailaSlotFillingSettings.stopOnAnyIntentThreshold &&
            inference.intent.name != ctx.initialActivatorContext.caila?.intent
        ) {
            return true
        }

        return false
    }

    private fun restoreContext(
        botContext: BotContext,
        initialActivationContext: ActivatorContext?
    ): CailaSlotFillingContext {
        var ctx = getSlotFillingContext(botContext)
        if (ctx == null) {
            initialActivationContext ?: error("Null activation context for caila slotfilling. It should never happen.")
            ctx = CailaSlotFillingContext.createInitial(initialActivationContext)
        }
        return ctx
    }

    private fun getSlotFillingContext(botContext: BotContext): CailaSlotFillingContext? {
        return botContext.session[SLOTFILLING_CONTEXT_KEY] as? CailaSlotFillingContext
    }

    private fun saveSlotFillingContext(botContext: BotContext, cailaSlotFillingContext: CailaSlotFillingContext) {
        botContext.session[SLOTFILLING_CONTEXT_KEY] = cailaSlotFillingContext
    }

    private fun clearSlotFillingContext(botContext: BotContext) {
        botContext.session.remove(SLOTFILLING_CONTEXT_KEY)
    }

    companion object {
        private const val SLOTFILLING_CONTEXT_KEY = "com/justai/jaicf/activator/caila/slotfilling/context"
    }
}

private val CailaSlotFillingContext.filledSlots: List<String>
    get() = knownSlots.map { it.name }

private val CailaSlotFillingContext.filledValues: List<String>
    get() = knownEntities.map { "${it.value}-${it.startPos}" }

private fun CailaSlotFillingContext.slotForEntity(e: CailaEntityMarkupData) =
    slots.filter { s -> s.entity == e.entity }
