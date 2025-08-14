package com.justai.jaicf.activator.llm.tracing

/**
 * Configuration for OpenTelemetry tracing
 */
data class OpenTelemetryConfig(
    val enabled: Boolean = false,
    val endpoint: String? = null,
    val serviceName: String = "jaicf-llm"
) {
    companion object {
        fun fromEnvironment(): OpenTelemetryConfig {
            val oTelEnabled = System.getenv("OTEL_TRACES_ENABLED") == "true" ||
                             System.getenv("OTEL_TRACES_SAMPLER") != null ||
                             System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") != null ||
                             System.getenv("ADK_TELEMETRY") == "otel"
            
            val endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
            
            return OpenTelemetryConfig(
                enabled = oTelEnabled,
                endpoint = endpoint
            )
        }
    }
}
