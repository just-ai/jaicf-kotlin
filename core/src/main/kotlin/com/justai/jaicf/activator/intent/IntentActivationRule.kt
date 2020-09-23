package com.justai.jaicf.activator.intent

import com.justai.jaicf.model.activation.ActivationRule

abstract class IntentActivationRule(val intentMatches: (String) -> Boolean): ActivationRule

open class IntentByNameActivationRule(val intent: String): IntentActivationRule({ it == intent })

class AnyIntentActivationRule: IntentActivationRule({ true })