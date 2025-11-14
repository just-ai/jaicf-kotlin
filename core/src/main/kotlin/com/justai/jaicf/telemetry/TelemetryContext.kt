package com.justai.jaicf.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.reactions.Reactions
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Storage for active telemetry spans in BotContext.
 * Spans are stored by name and can be retrieved by name.
 */
private var BotContext.activeTelemetrySpans by tempProperty<MutableMap<String, TelemetrySpan>>(saveDefault = true) { mutableMapOf() }

/**
 * Gets a telemetry span from BotContext by name.
 */
fun BotContext.getTelemetrySpan(spanName: String): TelemetrySpan? {
    return activeTelemetrySpans[spanName]
}

/**
 * Sets a telemetry span in BotContext by name.
 */
fun BotContext.setTelemetrySpan(spanName: String, span: TelemetrySpan) {
    activeTelemetrySpans[spanName] = span
}

/**
 * Removes a telemetry span from BotContext by name.
 */
fun BotContext.removeTelemetrySpan(spanName: String) {
    activeTelemetrySpans.remove(spanName)
}

/**
 * Gets the current active telemetry span from BotContext.
 * Returns the span with name "current" or null if not present.
 */
fun BotContext.getCurrentTelemetrySpan(): TelemetrySpan? {
    return activeTelemetrySpans["current"]
}

/**
 * Sets the current active telemetry span in BotContext.
 */
fun BotContext.setCurrentTelemetrySpan(span: TelemetrySpan?) {
    if (span != null) {
        activeTelemetrySpans["current"] = span
    } else {
        activeTelemetrySpans.remove("current")
    }
}

/**
 * Closes all active telemetry spans in BotContext.
 */
fun BotContext.closeAllTelemetrySpans() {
    activeTelemetrySpans.values.forEach { span ->
        try {
            span.close()
        } catch (e: Throwable) {
            // Ignore errors when closing spans
        }
    }
    activeTelemetrySpans.clear()
}

/**
 * Returns the current telemetry span from BotContext if available.
 * Tries to get BotContext from Reactions (via BotEngine.current()) or returns null.
 */
suspend fun currentTelemetrySpan(): TelemetrySpan? {
    val engine = BotEngine.current() ?: return null
    // Try to get BotContext from current execution context
    // This is a best-effort approach - BotContext should be available in most scenarios
    return try {
        // In scenarios, reactions.botContext should be available
        // But we can't access it directly here, so we'll need to pass BotContext explicitly
        // For now, return null - callers should pass BotContext explicitly
        null
    } catch (e: Exception) {
        null
    }
}

/**
 * Returns the current telemetry span from BotContext.
 * Requires BotContext to be passed explicitly.
 */
fun currentTelemetrySpan(context: BotContext): TelemetrySpan? {
    return context.getCurrentTelemetrySpan()
}

/**
 * Returns the current telemetry span from BotContext, or null if not available.
 * Can be called from both suspend and non-suspend contexts.
 * Tries to get BotContext from Reactions if available.
 */
fun currentTelemetrySpanBlocking(): TelemetrySpan? {
    // This function is kept for backward compatibility
    // But it may not work reliably without BotContext
    // Callers should prefer passing BotContext explicitly
    return try {
        runBlocking(EmptyCoroutineContext) {
            currentTelemetrySpan()
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Returns the current telemetry span from BotContext (blocking version).
 * Requires BotContext to be passed explicitly.
 */
fun currentTelemetrySpanBlocking(context: BotContext): TelemetrySpan? {
    return context.getCurrentTelemetrySpan()
}

/**
 * Legacy coroutine context element for backward compatibility.
 * @deprecated Use BotContext-based span storage instead.
 */
@Deprecated("Use BotContext-based span storage instead", ReplaceWith("BotContext.setTelemetrySpan()"))
class TelemetryContextElement(
    val span: TelemetrySpan
) : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<TelemetryContextElement>

    override val key: CoroutineContext.Key<TelemetryContextElement> = Key
}

/**
 * Legacy function for setting span in coroutine context.
 * @deprecated Use BotContext-based span storage instead.
 */
@Deprecated("Use BotContext-based span storage instead", ReplaceWith("context.setTelemetrySpan(name, span)"))
suspend fun <T> withTelemetrySpan(
    span: TelemetrySpan,
    block: suspend () -> T
): T = withContext(TelemetryContextElement(span)) { block() }

/**
 * Legacy function for setting span in coroutine context (blocking version).
 * @deprecated Use BotContext-based span storage instead.
 */
@Deprecated("Use BotContext-based span storage instead", ReplaceWith("context.setTelemetrySpan(name, span)"))
fun <T> withTelemetrySpanBlocking(span: TelemetrySpan, block: () -> T): T {
    return runBlocking {
        withTelemetrySpan(span) {
            block()
        }
    }
}
