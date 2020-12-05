package com.justai.jaicf.slotfilling

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation

private const val SLOTFILLING_ACTIVATOR_KEY = "com/justai/jaicf/slotfilling/activator"
private const val SLOTFILLING_NEXT_STATE_KEY = "com/justai/jaicf/slotfilling/nextState"

internal data class SlotFillingContext(
    val activator: Activator,
    val nextState: String,
    val activatorContext: ActivatorContext?
) {

    fun BotEngine.finishSlotFilling(botContext: BotContext, finished: SlotFillingFinished): ActivationContext {
        cancelSlotFilling(botContext)
        return ActivationContext(
            activator = activator,
            activation = Activation(nextState, finished.activatorContext)
        )
    }

    fun BotEngine.cancelSlotFilling(botContext: BotContext) {
        botContext.session -= listOf(SLOTFILLING_ACTIVATOR_KEY, SLOTFILLING_NEXT_STATE_KEY)
    }

    private fun toActivationContext(activatorContext: ActivatorContext) = ActivationContext(
        activator = activator,
        activation = Activation(nextState, activatorContext)
    )

}

internal fun BotEngine.startSlotFilling(botContext: BotContext, context: ActivationContext) =
    SlotFillingContext(context.activator, context.activation.state, context.activation.context).also {
        save(botContext, it)
    }

internal fun BotEngine.getSlotFillingContext(botContext: BotContext) = load(botContext)

internal fun BotEngine.isActiveSlotFilling(botContext: BotContext) = getSlotFillingContext(botContext) != null

private fun BotEngine.save(botContext: BotContext, context: SlotFillingContext) {
    botContext.session[SLOTFILLING_ACTIVATOR_KEY] = context.activator.name
    botContext.session[SLOTFILLING_NEXT_STATE_KEY] = context.nextState
}

private fun BotEngine.load(botContext: BotContext): SlotFillingContext? {
    val activatorName = botContext.session[SLOTFILLING_ACTIVATOR_KEY] as? String ?: return null
    val nextState = botContext.session[SLOTFILLING_NEXT_STATE_KEY] as? String ?: return null
    val activator = getActivatorForName(activatorName) ?: error("Cannot find slot-filling activator")
    return SlotFillingContext(activator, nextState, null)
}