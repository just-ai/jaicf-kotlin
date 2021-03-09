package com.justai.jaicf.activator.intent

import com.justai.jaicf.model.activation.ActivationRule

abstract class IntentActivationRule(val matches: (IntentActivatorContext) -> Boolean): ActivationRule

open class IntentByNameActivationRule(val intent: String): IntentActivationRule({ it.intent == intent })

class AnyIntentActivationRule(val except: List<String> = mutableListOf()): IntentActivationRule({ it.intent !in except })