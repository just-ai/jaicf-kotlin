package com.justai.jaicf.telemetry

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.sessionProperty
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.telemetry.TelemetrySpan.Companion.NoOp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Storage for active telemetry spans in BotContext.
 * Spans are stored by name and can be retrieved by name.
 */
private var BotContext.activeTelemetrySpans by tempProperty<MutableMap<String, TelemetrySpan>>(saveDefault = true) { mutableMapOf() }
private var BotContext.internalTelemetrySessionId by sessionProperty { "" }
private var BotContext.sessionTelemetrySpan by sessionProperty { NoOp }


/**
 * Gets a telemetry span from BotContext by name.
 */
fun BotContext.getTelemetrySpan(spanName: String): TelemetrySpan? {
    return activeTelemetrySpans[spanName]
}

fun BotContext.setTelemetrySessionId(sessionId: String) {
    internalTelemetrySessionId = sessionId
}

fun BotContext.getTelemetrySessionId(): String {
    return internalTelemetrySessionId
}

fun BotContext.getHandoffSpan(): TelemetrySpan? {
    return activeTelemetrySpans.entries
        .firstOrNull { it.key.lowercase().contains("handoff") }
        ?.value
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

fun BotContext.removeAllTelemetrySpan(name: String) {
    activeTelemetrySpans.keys
        .filter { it.contains(name) }
        .forEach { activeTelemetrySpans.remove(it) }
}

/**
 * Gets the current active telemetry span from BotContext.
 * Returns the span with name "current" or null if not present.
 */
fun BotContext.getCurrentTelemetrySpan(): TelemetrySpan? {
    return activeTelemetrySpans["current"]
}

fun BotContext.getSessionSpan(): TelemetrySpan {
    return sessionTelemetrySpan
}

fun BotContext.setSessionSpan(span: TelemetrySpan) {
    sessionTelemetrySpan = span
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
 * Returns the current telemetry span from BotContext.
 * Requires BotContext to be passed explicitly.
 */
fun currentTelemetrySpan(context: BotContext): TelemetrySpan? {
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
