package com.justai.jaicf.activator.llm.action

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.telemetry.GenAIAttributes
import com.justai.jaicf.activator.llm.telemetry.LLMAttributes
import com.justai.jaicf.activator.llm.tool.LLMToolInterruptionException
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioDsl
import com.justai.jaicf.builder.StateBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.currentState
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.telemetry.currentTelemetrySpan
import com.justai.jaicf.telemetry.getTelemetrySessionId
import com.justai.jaicf.telemetry.isNoOp
import com.justai.jaicf.telemetry.runWithTelemetry
import org.slf4j.LoggerFactory

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

    val provider = BotEngine.current()?.telemetryProvider ?: com.justai.jaicf.telemetry.TelemetryProvider.NoOp
    val spanName = "${GenAIAttributes.OPERATION_INVOKE_AGENT} ${state.path}"
    val attributes = mutableMapOf<String, Any?>(
        LLMAttributes.AGENT_STATE to state.path.toString(),
        LLMAttributes.AGENT_INPUT_LENGTH to request.input.length,
        GenAIAttributes.OPERATION_NAME to GenAIAttributes.OPERATION_INVOKE_AGENT,
        GenAIAttributes.AGENT_NAME to state.path.toString(),
    )
    context.getTelemetrySessionId().takeIf { it.isNotEmpty() }?.let {
        attributes[GenAIAttributes.CONVERSATION_ID] = it
    }

    val parent = currentTelemetrySpan()
    LoggerFactory.getLogger("jaicf.telemetry.debug").info(
        "[TELEMETRY] invoke_agent spanName=$spanName parent=${parent?.let { if (it.isNoOp()) "NoOp" else "${it::class.simpleName}#${System.identityHashCode(it)}" } ?: "null"}"
    )

    try {
        runWithTelemetry(provider, spanName, attributes) {
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