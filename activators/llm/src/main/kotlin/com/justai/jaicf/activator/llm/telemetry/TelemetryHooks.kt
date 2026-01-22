package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.hook.HookStage
import com.justai.jaicf.hook.TelemetryHook
import com.justai.jaicf.model.state.State
import com.justai.jaicf.reactions.Reactions
import com.openai.models.completions.CompletionUsage

enum class HookType {
    AGENT_INVOKE,
    LLM_CALL,
    TOOL_CALL,
    TOOL_CALLS,
    STREAMING,
    TOOL_EXECUTE
}

interface LLMTelemetryHook : TelemetryHook {
    val hookType: HookType
}

interface LifecycleHook : LLMTelemetryHook, BotActionHook {
    override val state: State
    override val context: BotContext
    override val request: BotRequest
    override val reactions: Reactions
    override val activator: ActivatorContext
    val attributes: Map<String, Any?>

    override fun withStage(stage: HookStage, exception: Throwable?): LifecycleHook
}

interface SimpleLifecycleHook : LLMTelemetryHook {
    override val context: BotContext
    val request: BotRequest
    val attributes: Map<String, Any?>

    override fun withStage(stage: HookStage, exception: Throwable?): SimpleLifecycleHook
}

data class AgentInvokeHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: HookStage = HookStage.START,
    override val exception: Throwable? = null,
) : LifecycleHook {
    override val hookType: HookType = HookType.AGENT_INVOKE

    override fun withStage(stage: HookStage, exception: Throwable?): AgentInvokeHook =
        copy(stage = stage, exception = exception)
}

data class LLMCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: HookStage = HookStage.START,
    override val exception: Throwable? = null,
    val completionUsage: CompletionUsage? = null,
) : LifecycleHook {
    override val hookType: HookType = HookType.LLM_CALL

    override fun withStage(stage: HookStage, exception: Throwable?): LLMCallHook =
        copy(stage = stage, exception = exception)

    fun withCompletionUsage(usage: CompletionUsage?): LLMCallHook =
        copy(completionUsage = usage)
}

data class ToolCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: HookStage = HookStage.START,
    override val exception: Throwable? = null,
) : LifecycleHook {
    override val hookType: HookType = HookType.TOOL_CALL

    override fun withStage(stage: HookStage, exception: Throwable?): ToolCallHook =
        copy(stage = stage, exception = exception)
}

data class ToolCallsHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: HookStage = HookStage.START,
    override val exception: Throwable? = null,
) : LifecycleHook {
    override val hookType: HookType = HookType.TOOL_CALLS

    override fun withStage(stage: HookStage, exception: Throwable?): ToolCallsHook =
        copy(stage = stage, exception = exception)
}

data class StreamingHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: HookStage = HookStage.START,
    override val exception: Throwable? = null,
) : LifecycleHook {
    override val hookType: HookType = HookType.STREAMING

    override fun withStage(stage: HookStage, exception: Throwable?): StreamingHook =
        copy(stage = stage, exception = exception)
}

data class ToolExecuteHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: HookStage = HookStage.START,
    override val exception: Throwable? = null,
) : SimpleLifecycleHook {
    override val hookType: HookType = HookType.TOOL_EXECUTE

    override fun withStage(stage: HookStage, exception: Throwable?): ToolExecuteHook =
        copy(stage = stage, exception = exception)
}

data class HandoffStartHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook

