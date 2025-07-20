package com.justai.jaicf.activator.llm.action

import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.tool.LLMToolInterruptionException
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.StateBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions


typealias LLMActionBlock = suspend LLMActionContext<out ActivatorContext, out BotRequest, out Reactions>.() -> Unit

val DefaultLLMActionBlock: LLMActionBlock = {
    reactions.sayFinalContent()
}

private suspend fun <A: ActivatorContext, B: BotRequest, R: Reactions> ActionContext<A, B, R>.llmAction(
    props: LLMPropsBuilder,
    body: LLMActionBlock,
) {
    val llm = LLMActionAPI.get.createContext(context, request, props)
    val context = LLMActionContext(context, activator, request, reactions, llm)

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