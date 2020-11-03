package com.justai.jaicf.activator.event

import com.justai.jaicf.model.activation.ActivationRule

abstract class EventActivationRule(val eventMatches: (String) -> Boolean): ActivationRule

open class EventByNameActivationRule(val event: String): EventActivationRule({ it == event})

class AnyEventActivationRule(val except: MutableList<String> = mutableListOf()): EventActivationRule({ it !in except })