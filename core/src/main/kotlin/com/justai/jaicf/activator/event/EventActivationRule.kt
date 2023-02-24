package com.justai.jaicf.activator.event

import com.justai.jaicf.model.activation.ActivationRuleAdapter

abstract class EventActivationRule(val matches: (EventActivatorContext) -> Boolean): ActivationRuleAdapter()

open class EventByNameActivationRule(val event: String): EventActivationRule({ it.event == event})

class AnyEventActivationRule(val except: MutableList<String> = mutableListOf()): EventActivationRule({ it.event !in except })