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

    var slots = result.inference.variants[0].slots?.map { it.name to it.value }?.toMap() ?: emptyMap()

    val entities = result.entitiesLookup.entities

    val topIntent = result.inference.variants[0].intent
}

val ActivatorContext.caila
    get() = this as? CailaIntentActivatorContext