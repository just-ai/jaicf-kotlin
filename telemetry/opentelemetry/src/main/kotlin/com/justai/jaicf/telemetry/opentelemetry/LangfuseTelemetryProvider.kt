package com.justai.jaicf.telemetry.opentelemetry

import com.justai.jaicf.telemetry.TelemetryProvider
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler

/**
 * Creates [TelemetryProvider] configured for [Langfuse](https://langfuse.com) via OTLP HTTP.
 *
 * Langfuse does not support gRPC; this provider uses OTLP HTTP exporter.
 * Langfuse maps GenAI semantic conventions (gen_ai.*, llm.*) to its data model.
 *
 * Local (self-hosted): default endpoint http://localhost:3000/api/public/otel
 * Cloud: use endpoint = "https://cloud.langfuse.com/api/public/otel" (EU) or "https://us.cloud.langfuse.com/api/public/otel" (US)
 *
 * Auth: set LANGFUSE_PUBLIC_KEY and LANGFUSE_SECRET_KEY, or LANGFUSE_AUTH_HEADER = "Basic " + Base64(pk:sk)
 *
 */
object LangfuseTelemetryProvider {

    private const val DEFAULT_ENDPOINT = "http://localhost:3000/api/public/otel"
    private const val COLLECTOR_ENDPOINT = "http://localhost:4318"
    private const val DEFAULT_SERVICE_NAME = "JAICF Bot"
    private const val DEFAULT_SERVICE_VERSION = "1.0.0"
    private const val DEFAULT_ENVIRONMENT = "development"

    fun create(
        endpoint: String = run {
            val useCollector = System.getenv("LANGFUSE_USE_COLLECTOR") == "true"
            when {
                useCollector -> System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: COLLECTOR_ENDPOINT
                else -> System.getenv("LANGFUSE_ENDPOINT") ?: DEFAULT_ENDPOINT
            }
        },
        authHeader: String? = run {
            if (System.getenv("LANGFUSE_USE_COLLECTOR") == "true") null
            else System.getenv("LANGFUSE_AUTH_HEADER")
                ?: run {
                    val publicKey = System.getenv("LANGFUSE_PUBLIC_KEY") ?: ""
                    val secretKey = System.getenv("LANGFUSE_SECRET_KEY") ?: ""
                    if (publicKey.isNotEmpty() && secretKey.isNotEmpty()) {
                        "Basic " + java.util.Base64.getEncoder().encodeToString("$publicKey:$secretKey".toByteArray())
                    } else null
                }
        },
        serviceName: String = DEFAULT_SERVICE_NAME,
        serviceVersion: String = DEFAULT_SERVICE_VERSION,
        environment: String = DEFAULT_ENVIRONMENT,
        addLoggingExporter: Boolean = false,
    ): TelemetryProvider {
        val tracesEndpoint = endpoint.removeSuffix("/") + "/v1/traces"
        val resource = Resource.create(
            Attributes.builder()
                .put(AttributeKey.stringKey("service.name"), serviceName)
                .put(AttributeKey.stringKey("service.version"), serviceVersion)
                .put(AttributeKey.stringKey("deployment.environment"), environment)
                .build()
        )
        val httpExporterBuilder = OtlpHttpSpanExporter.builder().setEndpoint(tracesEndpoint)
        authHeader?.let { httpExporterBuilder.addHeader("Authorization", it) }
        val spanProcessors = mutableListOf(
            SimpleSpanProcessor.create(httpExporterBuilder.build())
        )
        if (addLoggingExporter) {
            spanProcessors.add(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
        }
        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .setSampler(Sampler.alwaysOn())
            .apply { spanProcessors.forEach { addSpanProcessor(it) } }
            .build()
        val sdk = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build()
        return OpenTelemetryTelemetryProvider(sdk.getTracer(serviceName, serviceVersion))
    }
}
