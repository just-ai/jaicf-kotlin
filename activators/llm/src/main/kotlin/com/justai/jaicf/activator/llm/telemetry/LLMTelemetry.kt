package com.justai.jaicf.activator.llm.telemetry

/**
 * Provider-agnostic telemetry API for LLM activator.
 * Concrete implementation may bridge to core TelemetryProvider or OpenTelemetry.
 */
interface LLMTelemetryProvider {
    fun startSpan(
        name: String,
        kind: LLMSpanKind = LLMSpanKind.INTERNAL,
        parentContext: Any? = null,
        attributes: Map<String, Any?> = emptyMap(),
    ): LLMSpanHandle
}

/**
 * Singleton access to current provider used by LLM activator.
 */
object LLMTelemetry {
    @Volatile
    var provider: LLMTelemetryProvider = NoOpProvider

    object NoOpProvider : LLMTelemetryProvider {
        override fun startSpan(
            name: String,
            kind: LLMSpanKind,
            parentContext: Any?,
            attributes: Map<String, Any?>,
        ): LLMSpanHandle = NoOpSpanHandle
    }

    private object NoOpSpanHandle : LLMSpanHandle {
        override val id: String = "noop"
        override val context: Any? = null
        override fun setAttribute(key: String, value: String) {}
        override fun setAttribute(key: String, value: Boolean) {}
        override fun setAttribute(key: String, value: Number) {}
        override fun setAttributes(attrs: Map<String, Any?>) {}
        override fun addEvent(name: String, attributes: Map<String, Any?>) {}
        override fun end(status: LLMSpanStatus?) {}
    }
}