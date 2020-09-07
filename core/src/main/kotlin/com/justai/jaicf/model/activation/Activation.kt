package com.justai.jaicf.model.activation

import com.justai.jaicf.context.ActivatorContext

/**
 * The instance of this class produced by every [com.justai.jaicf.activator.Activator] once it handled a request.
 * Contains [ActivatorContext] with activation details and optional state that was found by the activator in scenario model.
 * Null state means that there is no state in the model related to the user's request. In this case such activation becomes a part of skippedActivators in [com.justai.jaicf.context.ActionContext].
 *
 * @property state an optional state of the dialogue scenario model related to the user's request
 * @property context an [ActivatorContext] that contains an activator-related details like named entities, confidence and etc.
 */
data class Activation(
    val state: String?,
    val context: ActivatorContext,
    val fromState: String
)