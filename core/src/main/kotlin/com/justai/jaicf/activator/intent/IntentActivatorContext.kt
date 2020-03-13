package com.justai.jaicf.activator.intent

import com.justai.jaicf.context.ActivatorContext

/**
 * Appears in the context of action block if some [IntentActivator] handled the user's request.
 * Particular intent activators extend this class and add some additional data like named entities. See for the concrete implementations like AlexaIntentActivatorContext, DialogflowActivatorContext and etc.
 *
 * @property intent a name of recognised intent
 */
open class IntentActivatorContext(
    override val confidence: Float,
    open val intent: String
): ActivatorContext

val ActivatorContext.intent
    get() = this as? IntentActivatorContext