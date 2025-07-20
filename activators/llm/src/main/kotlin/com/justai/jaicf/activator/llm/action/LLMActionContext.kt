package com.justai.jaicf.activator.llm.action

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.stream

data class LLMActionContext<A: ActivatorContext, B: BotRequest, R: Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: B,
    override val reactions: R,
    val llm: com.justai.jaicf.activator.llm.LLMContext,
) : ActionContext<A, B, R>(context, activator, request, reactions) {

    suspend fun Reactions.sayFinalContent() {
        llm.awaitFinalContent()?.let(::say)
    }

    suspend fun Reactions.streamOrSay() =
        stream?.say(llm.contentStream()) ?: llm.content()?.let(::say)
}
