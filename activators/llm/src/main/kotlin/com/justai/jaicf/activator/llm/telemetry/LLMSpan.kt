package com.justai.jaicf.activator.llm.telemetry

/**
 * Common span names used across LLM activator telemetry.
 */
object LLMSpanName {
    const val ActionInvoke = "LLM Invoke"
    const val LLMCall = "LLM Call"
    const val ToolCalls = "LLM ToolCalls"
    const val ToolCall = "LLM ToolCall"
    const val Handoff = "LLM Handoff"
    const val Streaming = "LLM Streaming"
}
