package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


@Serializable
data class CailaInferenceResultData(
    val intent: CailaIntentData,
    val confidence: Double,
    val slots: List<CailaKnownSlotData>?,
    val debug: JsonObject?
) : java.io.Serializable

@Serializable
data class CailaIntentData(
    private val id: Long,
    val path: String,
    val answer: String?,
    val customData: String?,
    val slots: List<CailaSlotData>?
)  : java.io.Serializable{
    val name = path.substring(path.lastIndexOf('/') + 1)
}

@Serializable
data class CailaSlotData(
    val name: String,
    val entity: String,
    val required: Boolean,
    val prompts: List<String>?,
    val array: Boolean?
) : java.io.Serializable

@Serializable
data class CailaKnownSlotData(
    val name: String,
    val value: String,
    val array: Boolean?
) : java.io.Serializable
