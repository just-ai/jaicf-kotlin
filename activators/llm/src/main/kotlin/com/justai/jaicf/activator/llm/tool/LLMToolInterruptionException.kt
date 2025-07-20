package com.justai.jaicf.activator.llm.tool

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions

typealias LLMToolInterruptCallback = suspend ActionContext<out ActivatorContext, out BotRequest, out Reactions>.() -> Unit

internal class LLMToolInterruptionException(
    val call: LLMToolCall<*>,
    val callback: LLMToolInterruptCallback,
) : Exception()

fun LLMToolCallContext<*>.interrupt(callback: LLMToolInterruptCallback): Nothing =
    throw LLMToolInterruptionException(call, callback)