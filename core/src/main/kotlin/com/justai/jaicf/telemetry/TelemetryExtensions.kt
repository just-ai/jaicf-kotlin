package com.justai.jaicf.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.context.BotContext

/**
 * Allows scenario actions to emit custom telemetry spans.
 *
 * ```
 * action {
 *     sendTelemetrySpan("jaicf.custom.event", mapOf("key" to "value")) {
 *         addEvent("payload.sent")
 *     }
 * }
 * ```
 *
 * @param name span name
 * @param attributes span attributes recorded on creation
 * @param block optional block executed with the created span as receiver
 */
suspend fun sendTelemetrySpan(
    name: String,
    attributes: TelemetryAttributes = emptyMap(),
    block: suspend TelemetrySpan.() -> Unit = {},
) {
    val engine = BotEngine.current()
    val provider = engine?.telemetryProvider ?: TelemetryProvider.NoOp
    
    // Try to get BotContext from Reactions if available
    // In scenarios, reactions.botContext should be available
    val botContext = try {
        // This is a best-effort approach
        // In most cases, BotContext should be available through reactions
        // But we can't access it directly here without passing it explicitly
        null
    } catch (e: Exception) {
        null
    }
    
    val parent = botContext?.let { currentTelemetrySpan(it) } ?: currentTelemetrySpan()
    val span = try {
        provider.createSpan(name, attributes, parent)
    } catch (e: Throwable) {
        TelemetrySpan.NoOp
    }

    try {
        // Set span in BotContext if available
        botContext?.let {
            it.setTelemetrySpan(name, span)
            it.setCurrentTelemetrySpan(span)
        }
        span.block()
    } catch (e: Throwable) {
        span.recordException(e)
        throw e
    } finally {
        span.close()
        botContext?.let {
            it.removeTelemetrySpan(name)
            if (it.getCurrentTelemetrySpan() == span) {
                it.setCurrentTelemetrySpan(null)
            }
        }
    }
}

/**
 * Sends a telemetry span with explicit BotContext.
 * This is the preferred way to use telemetry spans in scenarios.
 */
suspend fun sendTelemetrySpan(
    context: BotContext,
    name: String,
    attributes: TelemetryAttributes = emptyMap(),
    block: suspend TelemetrySpan.() -> Unit = {},
) {
    val engine = BotEngine.current()
    val provider = engine?.telemetryProvider ?: TelemetryProvider.NoOp
    val parent = currentTelemetrySpan(context)
    val span = try {
        provider.createSpan(name, attributes, parent)
    } catch (e: Throwable) {
        TelemetrySpan.NoOp
    }

    try {
        context.setTelemetrySpan(name, span)
        context.setCurrentTelemetrySpan(span)
        span.block()
    } catch (e: Throwable) {
        span.recordException(e)
        throw e
    } finally {
        span.close()
        context.removeTelemetrySpan(name)
        if (context.getCurrentTelemetrySpan() == span) {
            context.setCurrentTelemetrySpan(null)
        }
    }
}
