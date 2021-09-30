package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.caila.dto.CailaEntityMarkupData
import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.StrictActivatorContext

data class CailaEntityActivatorContext(
    val result: CailaAnalyzeResponseData,
    val entityData: CailaEntityMarkupData
) : StrictActivatorContext(), java.io.Serializable {

    val entity = entityData.entity

    companion object {
        private const val serialVersionUID = 1L
    }
}

val ActivatorContext.cailaEntity
    get() = this as? CailaEntityActivatorContext