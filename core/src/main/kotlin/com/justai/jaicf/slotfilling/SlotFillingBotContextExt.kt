package com.justai.jaicf.slotfilling

import com.justai.jaicf.context.BotContext

internal const val SLOTFILLING_ACTIVATOR_KEY = "com/justai/jaicf/slotfilling/activator"

internal fun BotContext.setSlotFillingActivator(activatorName: String?) {
    session[SLOTFILLING_ACTIVATOR_KEY] = activatorName
}

internal fun BotContext.getSlotFillingActivator() = session[SLOTFILLING_ACTIVATOR_KEY] as? String

internal fun BotContext.setSlotFillingIsFinished() = session.remove(SLOTFILLING_ACTIVATOR_KEY)

internal fun BotContext.isActiveSlotFilling() = getSlotFillingActivator() != null
