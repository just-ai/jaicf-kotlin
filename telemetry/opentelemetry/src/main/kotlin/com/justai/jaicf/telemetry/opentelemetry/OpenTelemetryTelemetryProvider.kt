package com.justai.jaicf.telemetry.opentelemetry

import com.justai.jaicf.telemetry.TelemetryAttributes
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.TelemetrySpan
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope

class OpenTelemetryTelemetryProvider(
    private val tracer: Tracer,
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

    override fun createSpan(
        name: String,
        attributes: TelemetryAttributes,
        parent: TelemetrySpan?
    ): TelemetrySpan {
        val builder = tracer.spanBuilder(name).applyParent(parent)
        val span = builder.startSpan()
        val scope = span.makeCurrent()
        attributes.forEach { (key, value) -> span.setDynamicAttribute(key, value) }
        return OtelTelemetrySpan(span, scope)
    }

    private fun SpanBuilder.applyParent(parent: TelemetrySpan?): SpanBuilder = when (parent) {
        is OtelTelemetrySpan -> setParent(Context.current().with(parent.unwrap()))
        else -> setParent(Context.current())
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