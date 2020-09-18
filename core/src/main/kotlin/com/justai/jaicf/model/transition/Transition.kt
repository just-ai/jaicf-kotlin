package com.justai.jaicf.model.transition

import com.justai.jaicf.model.activation.ActivationRule

data class Transition(
    val fromState: String,
    val toState: String,
    val rule: ActivationRule
)
