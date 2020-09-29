package com.justai.jaicf.activator

import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.state.ActivationTransition

data class ActivationContext(val activator: Activator?, val activation: Activation) {

    internal val toState = activation.transition?.toState?.toString()

    companion object Factory {
        fun createStrict(fromState: String, toState: String) = ActivationContext(
            activator = null,
            activation = Activation(ActivationTransition(fromState, toState), StrictActivatorContext())
        )
    }
}