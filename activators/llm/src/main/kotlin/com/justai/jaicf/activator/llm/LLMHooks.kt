package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.model.state.State
import com.justai.jaicf.reactions.Reactions

data class LLMToolCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val toolCallResult: LLMToolResult,
) : BotActionHook

data class LLMToolCallsHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val toolCallResults: List<LLMToolResult>,
) : BotActionHook