package com.justai.jaicf.activator.llm.telemetry

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.hook.BotActionHook
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.model.state.State
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.openai.models.completions.CompletionUsage

/**
 * Unified LLM telemetry hook with [type] determining span name and behavior.
 * All fields are present; nullable ones are null for types that don't need them (e.g. TOOL_EXECUTE).
 */
data class LLMLifecycleHook(
    val type: LLMSpanType,
    val state: State?,
    override val context: BotContext,
    val request: BotRequest,
    val reactions: Reactions?,
    val activator: ActivatorContext?,
    val attributes: Map<String, Any?> = emptyMap(),
    val completionUsage: CompletionUsage? = null,
    val toolCallResult: LLMToolResult? = null,
    val toolCallResults: List<LLMToolResult> = emptyList(),
) : BotHook {

    fun withCompletionUsage(usage: CompletionUsage?): LLMLifecycleHook =
        copy(completionUsage = usage)
}

/**
 * Resolves span display name per OpenTelemetry GenAI semantic conventions.
 * - LLM_CALL: chat {gen_ai.request.model}
 * - ACTION_INVOKE: invoke_agent {gen_ai.agent.name}
 * - TOOL_CALL/TOOL_EXECUTE: execute_tool {gen_ai.tool.name}
 */
fun LLMLifecycleHook.resolveSpanName(): String =
    when (type) {
        LLMSpanType.LLM_CALL -> {
            val model = attributes[LLMAttributes.MODEL] as? String ?: ""
            "${GenAIAttributes.OPERATION_CHAT} ${model.ifBlank { "unknown" }}"
        }
        LLMSpanType.ACTION_INVOKE -> {
            val agentName = state?.path?.toString() ?: "unknown"
            "${GenAIAttributes.OPERATION_INVOKE_AGENT} $agentName"
        }
        LLMSpanType.TOOL_CALL, LLMSpanType.TOOL_EXECUTE -> {
            val toolName = attributes[LLMAttributes.TOOL_NAME] as? String ?: "unknown"
            "${GenAIAttributes.OPERATION_EXECUTE_TOOL} $toolName"
        }
        else -> type.spanName
    }

/**
 * Storage key for span lookup. Uses fixed LLMSpanName for parent/context resolution;
 * dynamic names for tool spans (unique per tool).
 */
fun LLMLifecycleHook.getStorageKey(): String =
    when (type) {
        LLMSpanType.TOOL_CALL, LLMSpanType.TOOL_EXECUTE -> resolveSpanName()
        else -> type.spanName
    }

data class LLMHandoffHook(
    override val state: State,
    override val context: BotContext,
    override val request: BotRequest,
    override val reactions: Reactions,
    override val activator: ActivatorContext,
    val attributes: Map<String, Any?> = emptyMap(),
) : BotActionHook
