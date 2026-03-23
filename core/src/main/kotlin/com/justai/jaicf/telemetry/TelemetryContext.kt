package com.justai.jaicf.telemetry

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.sessionProperty
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.telemetry.TelemetrySpan.Companion.NoOp
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import org.slf4j.LoggerFactory

private val telemetryLog = LoggerFactory.getLogger("jaicf.telemetry.debug")

/**
 * Runs [block] with a new telemetry span. Parent is taken from [currentTelemetrySpan].
 * Span is closed automatically on block exit (success or exception).
 */
suspend fun <T> runWithTelemetry(
    provider: TelemetryProvider,
    name: String,
    attributes: TelemetryAttributes = emptyMap(),
    block: suspend (TelemetrySpan) -> T
): T {
    val parent = currentTelemetrySpan()
    telemetryLog.info("[TELEMETRY] runWithTelemetry name=$name parent=${parent?.let { if (it.isNoOp()) "NoOp" else "${it::class.simpleName}#${System.identityHashCode(it)}" } ?: "null"}")
    val span = provider.createSpanOrNoOp(name, attributes, parent?.realOrNull())
    return withContext(TelemetryContextElement(span.realOrNull())) {
        try {
            block(span)
        } catch (e: Throwable) {
            if (e !is TelemetrySkipException) span.recordException(e)
            throw e
        } finally {
            span.close()
        }
    }
}

/**
 * Runs [block] with [span] as current. Span is closed automatically on block exit.
 */
suspend fun <T> runWithTelemetry(
    span: TelemetrySpan,
    block: suspend (TelemetrySpan) -> T
): T = withContext(TelemetryContextElement(span.realOrNull())) {
    try {
        block(span)
    } catch (e: Throwable) {
        span.recordException(e)
        throw e
    } finally {
        span.close()
    }
}

private var BotContext.internalTelemetrySessionId by sessionProperty { "" }
private var BotContext.sessionTelemetrySpan by sessionProperty { NoOp }

/** Handoff span stored between processStates iterations (agent A creates, agent B consumes). */
private var BotContext.handoffSpanStorage by tempProperty<TelemetrySpan?>(saveDefault = true) { null }

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
 * Parent for new Handoff spans: from HandoffContextElement if inside handoff, else current span.
 * Keeps all Handoff spans as siblings under the request.
 */
suspend fun handoffSpanParent(context: BotContext): TelemetrySpan? =
    kotlinx.coroutines.currentCoroutineContext()[HandoffContextElement]?.parentBeforeHandoff
        ?: currentTelemetrySpan()

/**
 * Runs [block] with [span] as the current telemetry span in the coroutine context.
 * Nested spans created during [block] will use [span] as their parent.
 */
suspend fun <T> withTelemetrySpan(span: TelemetrySpan?, block: suspend () -> T): T =
    withContext(TelemetryContextElement(span?.takeIf { it != NoOp })) { block() }

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
    val current = currentTelemetrySpan()
    telemetryLog.info("[TELEMETRY] withOptionalHandoffSpan handoffPresent=${handoffSpan != null} currentSpan=${current?.let { if (it.isNoOp()) "NoOp" else "${it::class.simpleName}#${System.identityHashCode(it)}" } ?: "null"}")
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

fun BotContext.setTelemetrySessionId(sessionId: String) {
    internalTelemetrySessionId = sessionId
}

fun BotContext.getTelemetrySessionId(): String =
    internalTelemetrySessionId

fun BotContext.getHandoffSpan(): TelemetrySpan? = handoffSpanStorage

fun BotContext.setHandoffSpan(span: TelemetrySpan) {
    handoffSpanStorage = span
}

/**
 * Closes and removes the handoff span from BotContext (used after the handoff target agent finishes).
 */
fun BotContext.closeAndRemoveHandoffSpan() {
    handoffSpanStorage?.let { span ->
        try {
            span.close()
        } catch (e: Throwable) {
            // Ignore errors when closing
        }
        handoffSpanStorage = null
    }
}

/**
 * Closes handoff span if present (used on AnyError).
 */
fun BotContext.closeHandoffSpan() {
    closeAndRemoveHandoffSpan()
}

fun BotContext.getSessionSpan(): TelemetrySpan =
    sessionTelemetrySpan

fun BotContext.setSessionSpan(span: TelemetrySpan) {
    sessionTelemetrySpan = span
}
