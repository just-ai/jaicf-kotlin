package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

data class CailaIntentActivatorContext(
    val result: CailaAnalyzeResponseData
) : IntentActivatorContext(
    result.inference.variants.maxBy { it.confidence }!!.confidence.toFloat(),
    result.inference.variants.maxBy { it.confidence }!!.intent.name
) {

    val topIntent = result.inference.variants.maxBy { it.confidence }!!.intent

    var slots = result.inference.variants.maxBy { it.confidence }!!.slots?.map { it.name to it.value }?.toMap() ?: emptyMap()

    val entities = result.entitiesLookup.entities
}

val ActivatorContext.caila
    get() = this as? CailaIntentActivatorContext