package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.catchall.CatchAllActivatorContext
import com.justai.jaicf.context.ActivatorContext

class CailaCatchAllActivatorContext(
    val result: CailaAnalyzeResponseData,
) : CatchAllActivatorContext()

val ActivatorContext.cailaCatchAll
    get() = this as? CailaCatchAllActivatorContext
