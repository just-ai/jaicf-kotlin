package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.telemetry.opentelemetry.OpenTelemetryTelemetryProvider

/**
 * Example: run multi-agent LLM scenario with Jaeger/OTLP tracing.
 *
 * Telemetry spans include:
 * - LLM Invoke (per request)
 * - LLM Call, Streaming
 * - ToolCalls/ToolCall (when tools are used)
 * - Handoff (when agents transfer control)
 *
 * IMPORTANT! Set OPENAI_API_KEY and (optionally) OPENAI_BASE_URL before running.
 * Run Jaeger all-in-one locally: docker run -d -p 16686:16686 -p 4317:4317 jaegertracing/all-in-one
 * Then view traces at http://localhost:16686
 */
fun main() {
    val engine = BotEngine(HandoffScenario)
        .withTelemetry(OpenTelemetryTelemetryProvider())

    ConsoleChannel(engine).run("Calculate 2 + 2, Calculate 20 + 2  and tell me a joke")
}
