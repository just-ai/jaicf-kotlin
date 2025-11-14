package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.model.state.State
import com.justai.jaicf.reactions.Reactions
import com.openai.models.completions.CompletionUsage

data class BeforeLLMCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class StreamingFirstByteHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class AfterLLMCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val completionUsage: CompletionUsage? = null,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class AgentInvokeStartHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class AgentInvokeFinishHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class ToolCallsStartHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class ToolCallsFinishHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class ToolCallStartHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class ToolCallFinishHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class ToolCallErrorHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class HandoffStartHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

data class ToolExecuteStartHook(
    override val context: BotContext,
    val request: BotRequest,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotHook

data class ToolExecuteFinishHook(
    override val context: BotContext,
    val request: BotRequest,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotHook

data class ToolExecuteErrorHook(
    override val context: BotContext,
    val request: BotRequest,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotHook