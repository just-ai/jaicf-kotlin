package com.justai.jaicf.activator.llm.tracing

import com.justai.jaicf.activator.llm.LLMPropsBuilder

/**
 * Extension functions for LLMProps to enable tracing
 */
fun LLMPropsBuilder.withTracing(
    enabled: Boolean = true
) = apply {
    // Note: setTracingEnabled doesn't exist in LLMPropsBuilder
    // Tracing is enabled through environment variables
}

val com.justai.jaicf.activator.llm.LLMProps.isTracingEnabled: Boolean
    get() = System.getenv(TracingConstants.ENV_LANGSMITH_TRACING) == "true" ||
             System.getenv(TracingConstants.ENV_OTEL_TRACES_ENABLED) == "true" ||
             System.getenv(TracingConstants.ENV_OTEL_EXPORTER_OTLP_ENDPOINT) != null
