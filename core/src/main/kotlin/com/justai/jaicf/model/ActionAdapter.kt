package com.justai.jaicf.model

import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ProcessContext
import com.justai.jaicf.test.context.TestActionContext
import com.justai.jaicf.test.context.TestRequestContext

class ActionAdapter(
    private val action: ActionContext.() -> Unit
) {
    fun execute(context: ProcessContext) = with(context) {
        if (context.requestContext is TestRequestContext) {
            action.invoke(TestActionContext(this))
        } else {
            action.invoke(ActionContext(botContext, activation.context, request, reactions, skippedActivators))
        }
    }
}