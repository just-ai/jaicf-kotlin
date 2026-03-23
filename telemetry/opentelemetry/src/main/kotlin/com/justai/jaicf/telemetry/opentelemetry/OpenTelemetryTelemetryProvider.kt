package com.justai.jaicf.telemetry.opentelemetry

import com.justai.jaicf.telemetry.TelemetryAttributes
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.TelemetrySpan
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import org.slf4j.LoggerFactory

private val debugLog = LoggerFactory.getLogger("jaicf.telemetry.debug")

private object DebugSpanProcessor : SpanProcessor {
    override fun onStart(parentContext: Context, span: ReadWriteSpan) {}
    override fun onEnd(span: ReadableSpan) {
        val name = span.name
        val spanId = span.spanContext.spanId
        val parentSpanId = span.parentSpanContext.spanId
        val parentValid = span.parentSpanContext.isValid
        debugLog.info("[OTEL] span end name=$name spanId=$spanId parentSpanId=$parentSpanId parentValid=$parentValid")
    }
    override fun isStartRequired() = false
    override fun isEndRequired() = true
}

private const val DEFAULT_ENDPOINT = "http://localhost:4317"
private const val DEFAULT_SERVICE_NAME = "JAICF Bot"
private const val DEFAULT_SERVICE_VERSION = "1.0.0"
private const val DEFAULT_ENVIRONMENT = "development"

class OpenTelemetryTelemetryProvider(
    private val tracer: Tracer
) : TelemetryProvider {

    constructor(
        openTelemetry: OpenTelemetry,
        instrumentationName: String = DEFAULT_INSTRUMENTATION_NAME,
        instrumentationVersion: String? = null,
    ) : this(
        instrumentationVersion?.let { version ->
            openTelemetry.getTracer(instrumentationName, version)
        } ?: openTelemetry.getTracer(instrumentationName)
    )

    constructor(
        endpoint: String = DEFAULT_ENDPOINT,
        environment: String = DEFAULT_ENVIRONMENT,
        serviceName: String = DEFAULT_SERVICE_NAME,
        serviceVersion: String = DEFAULT_SERVICE_VERSION,
    ) : this(
        endpoint.let {
            val resource = Resource.create(
                Attributes.builder()
                    .put(AttributeKey.stringKey("service.name"), serviceName)
                    .put(AttributeKey.stringKey("service.version"), serviceVersion)
                    .put(AttributeKey.stringKey("deployment.environment"), environment)
                    .build()
            )

            val tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(Sampler.alwaysOn())
                .addSpanProcessor(DebugSpanProcessor)
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

            sdk.getTracer(serviceName, serviceVersion)
        }
    )

    override fun createSpan(
        name: String,
        attributes: TelemetryAttributes,
        parent: TelemetrySpan?
    ): TelemetrySpan {
        val span = when (parent) {
            is OtelTelemetrySpan -> {
                val parentContext = parent.unwrap().storeInContext(Context.root())
                val builder = tracer.spanBuilder(name).setParent(parentContext)
                val child = builder.startSpan()
                val childScope = child.makeCurrent()
                attributes.forEach { (key, value) -> child.setDynamicAttribute(key, value) }
                OtelTelemetrySpan(child, childScope)
            }
            else -> {
                val builder = tracer.spanBuilder(name).setNoParent()
                val child = builder.startSpan()
                val scope = child.makeCurrent()
                attributes.forEach { (key, value) -> child.setDynamicAttribute(key, value) }
                OtelTelemetrySpan(child, scope)
            }
        }
        return span
    }

    private class OtelTelemetrySpan(
        private val span: Span,
        private val scope: Scope,
    ) : TelemetrySpan {

        override fun setAttribute(key: String, value: Any?) {
            span.setDynamicAttribute(key, value)
        }

        override fun addEvent(name: String, attributes: TelemetryAttributes) {
            if (attributes.isEmpty()) {
                span.addEvent(name)
            } else {
                span.addEvent(name, attributes.toOtelAttributes())
            }
        }

        override fun recordException(exception: Throwable) {
            span.recordException(exception)
            span.setStatus(StatusCode.ERROR, exception.message ?: exception::class.java.simpleName ?: "error")
        }

        override fun close() {
            scope.close()
            span.end()
        }

        fun unwrap(): Span = span
    }

    companion object {
        private const val DEFAULT_INSTRUMENTATION_NAME = "com.justai.jaicf"
    }
}

private fun TelemetryAttributes.toOtelAttributes(): Attributes {
    if (isEmpty()) return Attributes.empty()
    val builder = Attributes.builder()
    forEach { (key, value) ->
        when (value) {
            null -> Unit
            is String -> builder.put(key, value)
            is Boolean -> builder.put(key, value)
            is Double -> builder.put(key, value)
            is Float -> builder.put(key, value.toDouble())
            is Long -> builder.put(key, value)
            is Int -> builder.put(key, value.toLong())
            is Short -> builder.put(key, value.toLong())
            is Byte -> builder.put(key, value.toLong())
            is Number -> builder.put(key, value.toDouble())
            is Iterable<*> -> {
                val strings = value.mapNotNull { it?.toString() }
                if (strings.isNotEmpty()) {
                    builder.put(AttributeKey.stringArrayKey(key), strings)
                }
            }

            else -> builder.put(key, value.toString())
        }
    }
    return builder.build()
}

private fun Span.setDynamicAttribute(key: String, value: Any?) {
    when (value) {
        null -> Unit
        is String -> setAttribute(key, value)
        is Boolean -> setAttribute(key, value)
        is Double -> setAttribute(key, value)
        is Float -> setAttribute(key, value.toDouble())
        is Long -> setAttribute(key, value)
        is Int -> setAttribute(key, value.toLong())
        is Short -> setAttribute(key, value.toLong())
        is Byte -> setAttribute(key, value.toLong())
        is Number -> setAttribute(key, value.toDouble())
        is Iterable<*> -> {
            val strings = value.mapNotNull { it?.toString() }
            if (strings.isNotEmpty()) {
                setAttribute(AttributeKey.stringArrayKey(key), strings)
            }
        }

        else -> setAttribute(key, value.toString())
    }
}