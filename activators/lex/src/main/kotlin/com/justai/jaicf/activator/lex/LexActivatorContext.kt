package com.justai.jaicf.activator.lex

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

open class LexActivatorContext(
    val intentData: LexIntentData.Recognized
) : IntentActivatorContext(intentData.confidence, intentData.intent) {
    val slots: Map<String, String?>
        get() = intentData.slots
}

val ActivatorContext.lex
    get() = this as? LexActivatorContext
