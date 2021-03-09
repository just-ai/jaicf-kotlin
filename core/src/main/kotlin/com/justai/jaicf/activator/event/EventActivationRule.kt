package com.justai.jaicf.activator.event

import com.justai.jaicf.model.activation.ActivationRule

abstract class EventActivationRule(val matches: (EventActivatorContext) -> Boolean): ActivationRule

open class EventByNameActivationRule(val event: String): EventActivationRule({ it.event == event})

class AnyEventActivationRule(val except: List<String> = mutableListOf()): EventActivationRule({ it.event !in except })