package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import com.justai.jaicf.activator.caila.dto.CailaIntentData
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

    var slots =
        result.inference.variants.maxBy { it.confidence }!!.slots?.map { it.name to it.value }?.toMap() ?: emptyMap()

    val entities = result.entitiesLookup.entities
}

val ActivatorContext.caila
    get() = this as? CailaIntentActivatorContext