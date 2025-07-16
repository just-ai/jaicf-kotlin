package com.justai.jaicf.model

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.context.TestActionContext
import com.justai.jaicf.test.context.TestRequestContext
import kotlinx.coroutines.runBlocking

class ActionAdapter(private val action: suspend ActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit) {

    fun execute(context: ProcessContext) = with(context) {
        val actionContext = if (context.requestContext is TestRequestContext) {
            TestActionContext(model, botContext, activationContext.activation.context, request, reactions, context.requestContext)
        } else {
            ActionContext(model, botContext, activationContext.activation.context, request, reactions)
        }

        runBlocking(context.executionContext.coroutineContext) {
            actionContext.action()
        }
    }

}