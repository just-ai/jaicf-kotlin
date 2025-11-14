package com.justai.jaicf.activator.llm.telemetry

/**
 * Domain span kinds used by LLM activator. The underlying provider maps these to its own kinds.
 */
enum class LLMSpanKind {
    INTERNAL,
    CLIENT,
    SERVER,
}

/**
 * Common span names used across LLM activator telemetry.
 */
object LLMSpanName {
    const val AgentInvoke = "LLM AgentInvoke"
    const val LLMCall = "LLM Call"
    const val LLMFirstChunk = "LLM First Chunk"
    const val ToolCalls = "ToolCalls"
    const val ToolCall = "ToolCall"
    const val Handoff = "Handoff"
    const val Agent = "Agent"
    const val Streaming = "Streaming"
    const val VectorStore = "VectorStore"
}

/**
 * A handle to the started span which allows adding attributes/events and finishing.
 */
interface LLMSpanHandle {
    val id: String
    val context: Any?

    fun setAttribute(key: String, value: String)
    fun setAttribute(key: String, value: Boolean)
    fun setAttribute(key: String, value: Number)
    fun setAttributes(attrs: Map<String, Any?>)

    fun addEvent(name: String, attributes: Map<String, Any?> = emptyMap())

    /**
     * Ends the span. Optional status may be provider-specific (e.g., OK/ERROR) expressed via attributes.
     */
    fun end(status: LLMSpanStatus? = null)
}

/**
 * Generic status for a span end. Provider may map this to its own status codes.
 */
data class LLMSpanStatus(
    val code: Code,
    val description: String? = null,
) {
    enum class Code { OK, ERROR, UNSET }
}


