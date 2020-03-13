package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaInferenceResults
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

data class CailaIntentActivatorContext(
    val result: CailaInferenceResults
) : IntentActivatorContext(result.confidence.toFloat(), result.intent.name) {
    val slots = result.slots?.map { it.name to it.value }?.toMap() ?: emptyMap()
}

val ActivatorContext.caila
    get() = this as? CailaIntentActivatorContext