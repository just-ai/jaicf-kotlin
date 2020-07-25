package com.justai.jaicf.activator.caila.slotfilling

/**
 * These settings are used in [CailaSlotfillingHelper] to specify when bot should end filling slots
 * and interrupt slot filling session.
 *
 * Slots are property of CailaIntent, required slots and prompts for then can be set in JAICP Web Interface.
 * If slot for intent is required, but no prompt is set, intent will not be activated.
 * Else if prompt is set, this prompt will be asked to client with no scenario changes.
 *
 * After client answered and filled all the slots, CailaIntentActivatorContext will contain filled slots.
 *
 * @see [com.justai.jaicf.activator.caila.CailaIntentActivatorContext]
 * @see [CailaSlotfillingHelper]
 * @see [com.justai.jaicf.slotfilling.SlotFiller]
 * @see [com.justai.jaicf.slotfilling.SlotFillingResult]
 *
 * @param maxSlotRetries specifies how many times client will be asked for slot.
 * @param stopOnAnyIntent specifies if slot filling session should be interrupted.
 * @param stopOnAnyIntentThreshold specifies threshold for intent interruption.
 * */
data class CailaSlotFillingSettings(
    val maxSlotRetries: Int,
    val stopOnAnyIntent: Boolean,
    val stopOnAnyIntentThreshold: Double
) {
    companion object {
        val DEFAULT = CailaSlotFillingSettings(2, false, 1.0)
    }
}