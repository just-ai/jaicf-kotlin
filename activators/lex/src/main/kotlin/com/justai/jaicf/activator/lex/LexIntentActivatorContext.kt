package com.justai.jaicf.activator.lex

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

open class LexIntentActivatorContext(
    intent: String,
    confidence: Float,
    val slots: Map<String, String?> = emptyMap()
) : IntentActivatorContext(confidence, intent)

val ActivatorContext.lex
    get() = this as? LexIntentActivatorContext