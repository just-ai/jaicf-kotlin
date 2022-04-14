package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

data class CailaIntentActivatorContext(
    val result: CailaAnalyzeResponseData,
    val intentData: CailaInferenceResultData
) : IntentActivatorContext(
    intent = intentData.intent.name,
    confidence = intentData.confidence.toFloat()
), java.io.Serializable {

    val topIntent = intentData.intent

    var slots = intentData.slots?.map { it.name to it.value }?.toMap() ?: emptyMap()

    val entities get() = result.entitiesLookup.entities

    companion object {
        private const val serialVersionUID = 4934755046273038374L
    }
}

val ActivatorContext.caila
    get() = this as? CailaIntentActivatorContext