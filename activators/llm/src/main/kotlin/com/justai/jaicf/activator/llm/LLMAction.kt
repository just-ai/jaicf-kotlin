package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.tool.LLMToolInterruptionException
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.StateBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions


typealias LLMActionContext = ActionContext<LLMActivatorContext, out BotRequest, out Reactions>
typealias LLMActionBlock = suspend LLMActionContext.() -> Unit

val DefaultLLMActionBlock: LLMActionBlock = {
    reactions.sayFinalContent(activator)
}

private suspend fun <A: ActivatorContext, B: BotRequest, R: Reactions> ActionContext<A, B, R>.llmAction(
    props: LLMPropsBuilder,
    body: LLMActionBlock,
) {
    val activator = LLMActivatorAPI.get.createActivatorContext(context, request, activator, props)
    val context = ActionContext(scenario, context, activator, request, reactions)

    try {
        body.invoke(context)
    } catch (e: LLMToolInterruptionException) {
        e.callback.invoke(context)
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