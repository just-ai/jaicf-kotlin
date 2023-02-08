package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


@Serializable
data class CailaInferenceResultData(
    val intent: CailaIntentData,
    val confidence: Double,
    val slots: List<CailaKnownSlotData>?,
    val debug: JsonObject?,
    val weights: InferenceResultWeights? = null
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 1698636910227622116L
    }
}

@Serializable
data class CailaIntentData(
    private val id: Long,
    val path: String,
    val answer: String?,
    val customData: String?,
    val slots: List<CailaSlotData>?
)  : java.io.Serializable{
    val name = path.substring(path.lastIndexOf('/') + 1)

    companion object {
        private const val serialVersionUID = -8682982605253378239L
    }
}

@Serializable
data class CailaSlotData(
    val name: String,
    val entity: String,
    val required: Boolean,
    val prompts: List<String>?,
    val array: Boolean?
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 8993441993535699579L
    }
}

@Serializable
data class CailaKnownSlotData(
    val name: String,
    val value: String,
    val array: Boolean?
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 1467148388316177817L
    }
}

@Serializable
data class InferenceResultWeights(
    val patterns: Double,
    val phrases: Double,
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 7438255714694047836L;
    }
}
