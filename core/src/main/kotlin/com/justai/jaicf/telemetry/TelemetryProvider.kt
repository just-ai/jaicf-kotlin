package com.justai.jaicf.telemetry

typealias TelemetryAttributes = Map<String, Any?>

interface TelemetryProvider {
    fun createSpan(
        name: String,
        attributes: TelemetryAttributes = emptyMap(),
        parent: TelemetrySpan? = null
    ): TelemetrySpan

    companion object {
        val NoOp: TelemetryProvider = object : TelemetryProvider {
            override fun createSpan(
                name: String,
                attributes: TelemetryAttributes,
                parent: TelemetrySpan?
            ): TelemetrySpan = TelemetrySpan.NoOp
        }
    }
}

interface TelemetrySpan : AutoCloseable {
    fun setAttribute(key: String, value: Any?)
    fun addEvent(name: String, attributes: TelemetryAttributes = emptyMap())
    fun recordException(exception: Throwable)

    override fun close()

    companion object {
        val NoOp: TelemetrySpan = object : TelemetrySpan {
            override fun setAttribute(key: String, value: Any?) = Unit
            override fun addEvent(name: String, attributes: TelemetryAttributes) = Unit
            override fun recordException(exception: Throwable) = Unit
            override fun close() = Unit
        }
    }
}