package com.justai.jaicf.activator.caila.slotfilling

data class CailaSlotFillingSettings(
    val maxSlotRetries: Int,
    val stopOnAnyIntent: Boolean,
    val stopOnAnyIntentThreshold: Double
) {
    companion object {
        val DEFAULT = CailaSlotFillingSettings(2, false, 1.0)
    }
}