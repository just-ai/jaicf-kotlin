package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CailaInferenceResults(
    val intent: CailaIntent,
    val confidence: Double,
    val slots: List<CailaSlot>?,
    val debug: JsonObject?
)

@Serializable
data class CailaIntent(
    private val id: Long,
    val path: String,
    val answer: String?,
    val customData: String?
) {
    val name = path.substring(path.lastIndexOf('/') + 1)
}

@Serializable
data class CailaSlot(
    val name: String,
    val value: String
)
