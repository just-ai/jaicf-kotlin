package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.slotfilling.CailaSlotFillingSettings

data class CailaNLUSettings(
    val accessToken: String,
    val confidenceThreshold: Double = 0.2,
    val cailaUrl: String = DEFAULT_CAILA_URL,
    val cailaSlotfillingSettings: CailaSlotFillingSettings = CailaSlotFillingSettings.DEFAULT
)

internal const val DEFAULT_CAILA_URL = "https://jaicf01-demo-htz.lab.just-ai.com/cailapub/api/caila/p"