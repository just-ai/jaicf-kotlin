package com.justai.jaicf.model.activation

data class ActivationRule(
    val fromState: String,
    val toState: String,
    val type: ActivationRuleType,
    val rule: String,
    val onlyFromThisState: Boolean = false
)