package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.slotfilling.CailaSlotFillingSettings

const val DEFAULT_CAILA_URL = "https://app.jaicp.com/cailapub"

data class CailaNLUSettings(
    val accessToken: String,
    val confidenceThreshold: Double = 0.2,
    val cailaUrl: String = DEFAULT_CAILA_URL,
    val cailaSlotfillingSettings: CailaSlotFillingSettings = CailaSlotFillingSettings.DEFAULT
    val cailaUrl: String = "$DEFAULT_CAILA_URL/api/caila/p"
)

internal const val DEFAULT_CAILA_URL = "https://jaicf01-demo-htz.lab.just-ai.com/cailapub/api/caila/p"