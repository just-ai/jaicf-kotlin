package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.telemetry.opentelemetry.OpenTelemetryTelemetryProvider
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.activator.llm.telemetry.installLLMActivatorTelemetryHooks
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler

private object OTelConfig {
    private const val SERVICE_NAME = "jaicf-llm-example"
    private const val SERVICE_VERSION = "1.0.0"

    fun buildTracerWithOtlp(endpoint: String = "http://localhost:4317"): Tracer {
        val resource = Resource.create(
            Attributes.builder()
                .put(AttributeKey.stringKey("service.name"), SERVICE_NAME)
                .put(AttributeKey.stringKey("service.version"), SERVICE_VERSION)
                .put(AttributeKey.stringKey("deployment.environment"), "development")
                .build()
        )

        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .setSampler(Sampler.alwaysOn())
            .addSpanProcessor(
                SimpleSpanProcessor.create(
                    OtlpGrpcSpanExporter.builder()
                        .setEndpoint(endpoint)
                        .build()
                )
            )
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .build()

        val sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build()

        return sdk.getTracer(SERVICE_NAME, SERVICE_VERSION)
    }
}

/**
 * Example: run multi-agent LLM scenario with Jaeger/OTLP tracing.
 * 
 * Telemetry spans include:
 * - LLM AgentInvoke (per request)
 * - LLM Call, Streaming
 * - ToolCalls/ToolCall (when tools are used)
 * - Handoff (when agents transfer control)
 * 
 * IMPORTANT! Set OPENAI_API_KEY and (optionally) OPENAI_BASE_URL before running.
 * Run Jaeger all-in-one locally: docker run -d -p 16686:16686 -p 4317:4317 jaegertracing/all-in-one
 * Then view traces at http://localhost:16686
 */
fun main() {
    val tracer = try {
        println("Connecting OTLP exporter at http://localhost:4317 ...")
        OTelConfig.buildTracerWithOtlp()
    } catch (e: Exception) {
        // fallback to console exporter only
        println("Failed to init OTLP exporter: ${'$'}{e.message}")
        OTelConfig.buildTracerWithOtlp("") // still returns tracer, logging processor enabled
    }

    val engine = BotEngine(HandoffScenario)
        .withTelemetry(OpenTelemetryTelemetryProvider(tracer))
        .apply { installLLMActivatorTelemetryHooks() }

    // Demo: triggers agent handoffs and tool calls, visible in Jaeger
    ConsoleChannel(engine).run("Calculate 2 + 2 and tell me a joke")
}


