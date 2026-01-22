package com.justai.jaicf.activator.llm.telemetry

/**
 * Common span names used across LLM activator telemetry.
 */
object LLMSpanName {
    const val AgentInvoke = "LLM AgentInvoke"
    const val LLMCall = "LLM Call"
    const val ToolCalls = "ToolCalls"
    const val ToolCall = "ToolCall"
    const val Handoff = "Handoff"
    const val Streaming = "Streaming"
}


