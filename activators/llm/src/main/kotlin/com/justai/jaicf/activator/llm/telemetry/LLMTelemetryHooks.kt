package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.telemetry.TelemetryHookStage
import com.justai.jaicf.telemetry.TelemetryHook
import com.justai.jaicf.model.state.State
import com.justai.jaicf.reactions.Reactions
import com.openai.models.completions.CompletionUsage

enum class LLMHookType {
    ACTION_INVOKE,
    LLM_CALL,
    TOOL_CALL,
    TOOL_CALLS,
    STREAMING,
    TOOL_EXECUTE,
}

interface LLMTelemetryHook : TelemetryHook {
    val type: LLMHookType
}

interface LLMLifecycleHook : LLMTelemetryHook, BotActionHook {
    override val state: State
    override val context: BotContext
    override val request: BotRequest
    override val reactions: Reactions
    override val activator: ActivatorContext
    val attributes: Map<String, Any?>

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMLifecycleHook
}

interface SimpleLifecycleHook : LLMTelemetryHook {
    override val context: BotContext
    val request: BotRequest
    val attributes: Map<String, Any?>

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): SimpleLifecycleHook
}

data class LLMActionHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
) : LLMLifecycleHook {
    override val type: LLMHookType = LLMHookType.ACTION_INVOKE

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMActionHook =
        copy(stage = stage, exception = exception)
}

data class LLMCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
    val completionUsage: CompletionUsage? = null,
) : LLMLifecycleHook {
    override val type: LLMHookType = LLMHookType.LLM_CALL

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMCallHook =
        copy(stage = stage, exception = exception)

    fun withCompletionUsage(usage: CompletionUsage?): LLMCallHook =
        copy(completionUsage = usage)
}

data class LLMToolCallHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
) : LLMLifecycleHook {
    override val type: LLMHookType = LLMHookType.TOOL_CALL

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMToolCallHook =
        copy(stage = stage, exception = exception)
}

data class LLMToolCallsHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
) : LLMLifecycleHook {
    override val type: LLMHookType = LLMHookType.TOOL_CALLS

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMToolCallsHook =
        copy(stage = stage, exception = exception)
}

data class LLMStreamingHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
) : LLMLifecycleHook {
    override val type: LLMHookType = LLMHookType.STREAMING

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMStreamingHook =
        copy(stage = stage, exception = exception)
}

data class LLMToolExecuteHook(
    override val context: BotContext,
    override val request: BotRequest,
    override val attributes: Map<String, Any?> = emptyMap(),
    override val stage: TelemetryHookStage = TelemetryHookStage.START,
    override val exception: Throwable? = null,
) : SimpleLifecycleHook {
    override val type: LLMHookType = LLMHookType.TOOL_EXECUTE

    override fun withStage(stage: TelemetryHookStage, exception: Throwable?): LLMToolExecuteHook =
        copy(stage = stage, exception = exception)
}

data class LLMHandoffHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook
