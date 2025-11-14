package com.justai.jaicf.examples.telemetry

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

/**
 * Configuration for OpenTelemetry tracing
 */
object TelemetryConfig {
    
    const val SERVICE_NAME = "jaicf-telemetry-example"
    const val SERVICE_VERSION = "1.0.0"
    
    /**
     * Build OpenTelemetry tracer with console logging exporter
     */
    fun buildTracerWithLogging(): Tracer {
        val resource = createResource()
        
        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .setSampler(Sampler.alwaysOn())
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .build()

        val sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build()

        return sdk.getTracer(SERVICE_NAME, SERVICE_VERSION)
    }
    
    /**
     * Build OpenTelemetry tracer with OTLP exporter (for Jaeger, etc.)
     * Exports to http://localhost:4317 by default
     */
    fun buildTracerWithOtlp(endpoint: String = "http://localhost:4317"): Tracer {
        val resource = createResource()
        
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
            // Also log to console
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .build()

        val sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build()

        return sdk.getTracer(SERVICE_NAME, SERVICE_VERSION)
    }
    
    private fun createResource(): Resource {
        return Resource.create(
            Attributes.builder()
                .put(AttributeKey.stringKey("service.name"), SERVICE_NAME)
                .put(AttributeKey.stringKey("service.version"), SERVICE_VERSION)
                .put(AttributeKey.stringKey("deployment.environment"), "development")
                .build()
        )
    }
}

