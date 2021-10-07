package com.justai.jaicf.activator.intent

import com.justai.jaicf.model.activation.ActivationRuleAdapter

abstract class IntentActivationRule(val matches: (IntentActivatorContext) -> Boolean): ActivationRuleAdapter()

open class IntentByNameActivationRule(val intent: String): IntentActivationRule({ it.intent == intent })
class AnyIntentActivationRule(val except: List<String> = mutableListOf()): IntentActivationRule({ it.intent !in except })