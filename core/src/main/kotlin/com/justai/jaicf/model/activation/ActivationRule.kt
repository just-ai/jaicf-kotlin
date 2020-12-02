package com.justai.jaicf.model.activation

import com.justai.jaicf.activator.catchall.CatchAllActivationRule
import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule

interface ActivationRule

internal val ActivationRule.priority: Int
    get() = when (this) {
        is AnyIntentActivationRule -> 0
        is AnyEventActivationRule -> 0
        is CatchAllActivationRule -> -1
        else -> 1
    }