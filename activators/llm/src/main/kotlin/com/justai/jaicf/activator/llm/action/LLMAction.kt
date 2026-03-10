package com.justai.jaicf.activator.llm.action

import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.telemetry.LLMLifecycleHook
import com.justai.jaicf.activator.llm.telemetry.LLMSpanType
import com.justai.jaicf.activator.llm.tool.LLMToolInterruptionException
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.StateBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.currentState
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.activator.llm.telemetry.LLMSpanName
import com.justai.jaicf.telemetry.runWithTelemetry


typealias LLMActionBlock = suspend LLMActionContext<out ActivatorContext, out BotRequest, out Reactions>.() -> Unit

val DefaultLLMActionBlock: LLMActionBlock = {
    reactions.sayFinalContent()
}

private suspend fun <A: ActivatorContext, B: BotRequest, R: Reactions> ActionContext<A, B, R>.llmAction(
    props: LLMPropsBuilder,
    body: LLMActionBlock,
) {
    val llm = LLMActionAPI.get.createContext(context, request, props)
    val actionContext = LLMActionContext(context, activator, request, reactions, llm)
    val state = context.currentState()!!

    try {
        runWithTelemetry(
            LLMLifecycleHook(
                type = LLMSpanType.ACTION_INVOKE,
                state = state,
                context = context,
                request = request,
                reactions = reactions,
                activator = activator
            ),
            LLMSpanName.ActionInvoke
        ) {
            body.invoke(actionContext)
        }
    } catch (e: LLMToolInterruptionException) {
        e.callback.invoke(this)
    }
}

fun <B : BotRequest, R : Reactions> StateBuilder<B, R>.llmAction(
    props: LLMPropsBuilder = DefaultLLMProps,
    body: @ScenarioDsl LLMActionBlock = DefaultLLMActionBlock,
) = action {
    llmAction(props, body)
}

fun <B : BotRequest, R : Reactions> StateBuilder<B, R>.llmAction(
    channelToken: ChannelTypeToken<B, R>,
    props: LLMPropsBuilder = DefaultLLMProps,
    body: @ScenarioDsl LLMActionBlock = DefaultLLMActionBlock,
) = action(channelToken) {
    llmAction(props, body)
}