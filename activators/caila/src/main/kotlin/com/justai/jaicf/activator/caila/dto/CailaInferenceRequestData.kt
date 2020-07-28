package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable

@Serializable
data class CailaAnalyzeRequestData(
    val data: CailaInferenceRequestData,
    val showAll: Boolean
)

@Serializable
data class CailaInferenceRequestData(
    val phrase: CailaPhraseMarkupData,
    val knownSlots: List<CailaKnownSlotData>,
    val nBest: Int,
    val showDebugInfo: Boolean = false
)

@Serializable
data class CailaPhraseMarkupData(
    val text: String,
    val entities: List<CailaEntityMarkupData>
)
