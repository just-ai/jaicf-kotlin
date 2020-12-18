package com.justai.jaicf.model

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.context.TestActionContext
import com.justai.jaicf.test.context.TestRequestContext

class ActionAdapter(private val action: ActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit) {

    fun execute(context: ProcessContext) = with(context) {
        val actionContext = if (context.requestContext is TestRequestContext) {
            TestActionContext(botContext, activationContext.activation.context, request, reactions, context.requestContext)
        } else {
            ActionContext(botContext, activationContext.activation.context, request, reactions)
        }

        actionContext.action()
    }

}