package com.justai.jaicf.activator.caila

import com.justai.jaicf.generic.ActivatorTypeToken

typealias CailaTypeToken = ActivatorTypeToken<CailaIntentActivatorContext>
typealias CailaEntityTypeToken = ActivatorTypeToken<CailaEntityActivatorContext>

val caila: CailaTypeToken = ActivatorTypeToken()
val cailaEntity: CailaEntityTypeToken = ActivatorTypeToken()