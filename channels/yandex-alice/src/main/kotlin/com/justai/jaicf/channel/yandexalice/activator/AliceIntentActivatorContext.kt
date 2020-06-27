package com.justai.jaicf.channel.yandexalice.activator

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.channel.yandexalice.api.Request
import com.justai.jaicf.context.ActivatorContext

class AliceIntentActivatorContext(
    intentName: String,
    intentSlots: Request.Nlu.Intent
): IntentActivatorContext(
    confidence = 1f,
    intent = intentName
) {
    val slots = intentSlots.slots
}

val ActivatorContext.alice
    get() = this as? AliceIntentActivatorContext