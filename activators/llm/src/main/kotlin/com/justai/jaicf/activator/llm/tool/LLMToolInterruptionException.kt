package com.justai.jaicf.activator.llm.tool

import com.justai.jaicf.activator.llm.action.LLMActionBlock

internal class LLMToolInterruptionException(
    val call: LLMToolCall<*>,
    val callback: LLMActionBlock,
) : Exception()

fun LLMToolCallContext<*>.interrupt(callback: LLMActionBlock): Nothing =
    throw LLMToolInterruptionException(call, callback)