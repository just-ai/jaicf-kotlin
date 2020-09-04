package com.justai.jaicf.activator

import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.model.activation.ActivationRule

interface ActivationRuleMatcher {
    fun match(rule: ActivationRule): ActivatorContext?
}