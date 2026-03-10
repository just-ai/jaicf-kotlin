package com.justai.jaicf.telemetry

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.sessionProperty
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.telemetry.TelemetrySpan.Companion.NoOp
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Storage for active telemetry spans in BotContext.
 * Spans are stored by name for lookup (e.g. to close or add attributes).
 *
 * **Hierarchy is built via CoroutineContext** — the current (parent) span is taken from
 * the coroutine context where the code runs, not from manual mapping in BotContext.
 * Use [withTelemetrySpan] to run a block with a span as the current parent for nested spans.
 */
private var BotContext.activeTelemetrySpans by tempProperty<MutableMap<String, TelemetrySpan>>(saveDefault = true) { mutableMapOf() }
private var BotContext.internalTelemetrySessionId by sessionProperty { "" }
private var BotContext.sessionTelemetrySpan by sessionProperty { NoOp }

/**
 * CoroutineContext element that holds the current telemetry span.
 */
class TelemetryContextElement(val span: TelemetrySpan?) : AbstractCoroutineContextElement(TelemetryContextElement) {
    companion object Key : CoroutineContext.Key<TelemetryContextElement>
}

/**
 * When running inside a handoff (Agent B after A handoffs), stores the span that was current
 * before entering the handoff. New Handoff spans use this as parent so they stay siblings.
 */
class HandoffContextElement(val parentBeforeHandoff: TelemetrySpan?) : AbstractCoroutineContextElement(HandoffContextElement) {
    companion object Key : CoroutineContext.Key<HandoffContextElement>
}

/**
 * Gets the current telemetry span from the coroutine context.
 * Must be called from a suspend context.
 */
suspend fun currentTelemetrySpan(): TelemetrySpan? =
    kotlinx.coroutines.currentCoroutineContext()[TelemetryContextElement]?.span?.takeIf { it != NoOp }

/**
 * Parent for new Handoff spans: from HandoffContextElement if inside handoff, else request span.
 * Keeps all Handoff spans as siblings under the request.
 */
suspend fun handoffSpanParent(context: BotContext): TelemetrySpan? {
    val handoffCtx = kotlinx.coroutines.currentCoroutineContext()[HandoffContextElement]
    return handoffCtx?.parentBeforeHandoff
        ?: context.getTelemetrySpan(TelemetrySpanName.BOT_REQUEST.value)
        ?: currentTelemetrySpan()
}

/**
 * Runs [block] with [span] as the current telemetry span in the coroutine context.
 * Nested spans created during [block] will use [span] as their parent.
 */
suspend fun <T> withTelemetrySpan(span: TelemetrySpan?, block: suspend () -> T): T =
    withContext(TelemetryContextElement(span?.takeIf { it != NoOp })) { block() }

/**
 * Runs [block] with [span] as current if [span] is not null and not NoOp, otherwise runs [block] without span.
 */
suspend inline fun <T> withOptionalSpan(span: TelemetrySpan?, noinline block: suspend () -> T): T =
    if (span != null && span != NoOp) withTelemetrySpan(span, block) else block()

/**
 * Runs [block] with [handoffSpan] as current span and [HandoffContextElement] so that
 * nested Handoff spans use the pre-handoff parent (keeping them siblings).
 */
suspend fun <T> withHandoffSpan(handoffSpan: TelemetrySpan, block: suspend () -> T): T {
    val parentBeforeHandoff = currentTelemetrySpan()
    return withContext(HandoffContextElement(parentBeforeHandoff)) {
        withTelemetrySpan(handoffSpan) { block() }
    }
}

/**
 * Runs [block] with handoff span context when [context] has an active handoff span,
 * otherwise runs [block] directly. Closes and removes the handoff span after the block when present.
 */
suspend fun <T> withOptionalHandoffSpan(context: BotContext, block: suspend () -> T): T {
    val handoffSpan = context.getHandoffSpan()
    return if (handoffSpan != null) {
        try {
            withHandoffSpan(handoffSpan, block)
        } finally {
            context.closeAndRemoveHandoffSpan()
        }
    } else {
        block()
    }
}

/**
 * Gets a telemetry span from BotContext by name.
 */
fun BotContext.getTelemetrySpan(spanName: String): TelemetrySpan? =
    activeTelemetrySpans[spanName]

fun BotContext.setTelemetrySessionId(sessionId: String) {
    internalTelemetrySessionId = sessionId
}

fun BotContext.getTelemetrySessionId(): String =
    internalTelemetrySessionId

fun BotContext.getHandoffSpan(): TelemetrySpan? =
    activeTelemetrySpans.entries
        .firstOrNull { it.key.lowercase().contains("handoff") }
        ?.value

/**
 * Closes and removes the handoff span from BotContext (used after the handoff target agent finishes).
 */
fun BotContext.closeAndRemoveHandoffSpan() {
    getHandoffSpan()?.let { span ->
        try {
            span.close()
        } catch (e: Throwable) {
            // Ignore errors when closing
        }
        activeTelemetrySpans.entries
            .firstOrNull { it.key.lowercase().contains("handoff") }
            ?.key
            ?.let { activeTelemetrySpans.remove(it) }
    }
}

/**
 * Flag set when handoff span is created; cleared when the creating agent finishes.
 * Prevents closing the handoff span too early (before the target agent runs).
 */
var BotContext.handoffPending by tempProperty { false }

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

fun BotContext.getSessionSpan(): TelemetrySpan =
    sessionTelemetrySpan

fun BotContext.setSessionSpan(span: TelemetrySpan) {
    sessionTelemetrySpan = span
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
