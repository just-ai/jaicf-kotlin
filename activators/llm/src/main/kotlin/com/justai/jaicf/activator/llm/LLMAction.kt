package com.justai.jaicf.activator.llm

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.StateBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions


typealias LLMActionBlock = ActionContext<LLMActivatorContext, *, *>.() -> Unit

val DefaultLLMActionBlock: LLMActionBlock = {
    activator.awaitFinalContent()?.also(reactions::say)
}

private fun <A: ActivatorContext, B: BotRequest, R: Reactions> ActionContext<A, B, R>.llmAction(
    props: LLMPropsBuilder,
    body: LLMActionBlock,
) {
    val ac = LLMActivatorAPI.get
        .createActivatorContext(props, context, request, activator)
    body.invoke(ActionContext(scenario, context, ac, request, reactions))
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