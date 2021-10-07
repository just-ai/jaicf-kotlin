package com.justai.jaicf.activator.regex

import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.activation.ActivationRuleAdapter

open class RegexActivationRule(val regex: String) : ActivationRuleAdapter()