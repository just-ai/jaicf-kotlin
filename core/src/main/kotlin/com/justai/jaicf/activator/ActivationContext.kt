package com.justai.jaicf.activator

import com.justai.jaicf.model.activation.Activation

data class ActivationContext(
    val activator: Activator,
    val activation: Activation
)