package com.justai.jaicf.activator.llm

import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.stream

suspend fun Reactions.sayFinalContent(activator: LLMActivatorContext) =
    activator.awaitFinalContent()?.let(::say)

suspend fun Reactions.streamOrSay(activator: LLMActivatorContext) =
    stream?.say(activator.contentStream()) ?: activator.content()?.let(::say)