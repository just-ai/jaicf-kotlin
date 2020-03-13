package com.justai.jaicf.channel.googleactions.dialogflow

import com.google.actions.api.ActionRequest
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.context.ActivatorContext

val ActivatorContext.actionsDialogflow
    get() = this as? ActionsDialogflowActivatorContext

data class ActionsDialogflowActivatorContext(
    val request: ActionRequest
): IntentActivatorContext(
    confidence = 1f,
    intent = request.intent
) {
    val slots = request.webhookRequest?.queryResult?.parameters ?: mapOf()
}