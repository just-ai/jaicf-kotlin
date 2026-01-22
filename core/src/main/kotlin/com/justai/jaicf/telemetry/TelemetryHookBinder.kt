package com.justai.jaicf.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.hook.AfterProcessHook
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BeforeProcessHook
import com.justai.jaicf.hook.RequestLifecycleStage
import com.justai.jaicf.hook.TelemetryStartProcessHook
import com.justai.jaicf.test.reactions.answer
import java.util.UUID

suspend inline fun <T> BotEngine.runWithTelemetry(
    request: BotRequest,
    requestContext: RequestContext,
    context: BotContext,
    crossinline block: suspend () -> T
): T {
    val startedAt = System.nanoTime()

    val sessionId = context.getTelemetrySessionId()
        .takeIf { it.isNotEmpty() }
        ?: UUID.randomUUID().toString().also { context.setTelemetrySessionId(it) }

    val sessionRootName = "jaicf.session"
    var sessionRootSpan = context.getSessionSpan()
    if (sessionRootSpan == TelemetrySpan.NoOp) {
        val sessionAttributes = mapOf(
            "session.id" to sessionId,
            "jaicf.request.client_id" to request.clientId,
        )
        sessionRootSpan = try {
            telemetryProvider.createSpan(sessionRootName, sessionAttributes, parent = null)
        } catch (e: Throwable) {
            print(e)
            TelemetrySpan.NoOp
        }

        if (sessionRootSpan != TelemetrySpan.NoOp) {
            context.setSessionSpan(sessionRootSpan)
            context.setTelemetrySpan(sessionRootName, sessionRootSpan)
        }
    }

    val attributes = mapOf(
        "jaicf.request.type" to request.type.name,
        "jaicf.request.client_id" to request.clientId,
        "jaicf.session.new" to requestContext.newSession,
        "session.id" to sessionId
    )

    val parentSpan = sessionRootSpan.takeUnless { it == TelemetrySpan.NoOp }
    val span = try {
        telemetryProvider.createSpan("jaicf.bot.request", attributes, parentSpan)
    } catch (e: Throwable) {
        TelemetrySpan.NoOp
    }

    if (span != TelemetrySpan.NoOp) {
        context.setTelemetrySpan("jaicf.bot.request", span)
        context.setCurrentTelemetrySpan(span)
    }

    return try {
        val startHook = TelemetryStartProcessHook(
            context,
            request,
            requestContext,
            RequestLifecycleStage.START,
            durationMillis(startedAt)
        )
        this.hooks.triggerHook(startHook)
        block()
    } catch (e: Throwable) {
        span.recordException(e)
        throw e
    } finally {
        val endHook = TelemetryStartProcessHook(
            context,
            request,
            requestContext,
            RequestLifecycleStage.END,
            durationMillis(startedAt)
        )
        this.hooks.triggerHook(endHook)

        if (span != TelemetrySpan.NoOp) {
            span.close()
            context.removeTelemetrySpan("jaicf.bot.request")
            if (context.getSessionSpan() != span) {
                val root = context.getTelemetrySpan(sessionRootName)
                context.setCurrentTelemetrySpan(root)
            }
        }
    }
}


internal fun BotEngine.installTelemetryHooks(telemetryProvider: TelemetryProvider = TelemetryProvider.NoOp) {

    hooks.addHookAction<TelemetryStartProcessHook> {
        TelemetryHookProcessor.handleRequestLifecycle(telemetryProvider, this)
    }

    hooks.addHookAction<BeforeProcessHook> {
        TelemetryHookProcessor.handleBeforeProcess(telemetryProvider, this)
    }

    hooks.addHookAction<AfterProcessHook> {
        TelemetryHookProcessor.handleAfterProcess(telemetryProvider, this)
    }

    hooks.addHookAction<ActionErrorHook> {
        TelemetryHookProcessor.handleActionError(telemetryProvider, this)
    }

    hooks.addHookAction<AnyErrorHook> {
        TelemetryHookProcessor.handleAnyError(telemetryProvider, this)
    }
}

private object TelemetryHookProcessor {

    fun handleRequestLifecycle(telemetryProvider: TelemetryProvider, hook: TelemetryStartProcessHook) {
        if (hook.stage == RequestLifecycleStage.START) {
            val name = "jaicf.request.start"
            val attributes = mapOf(
                "jaicf.request.input" to hook.request.input,
                "jaicf.request.type" to hook.request.type.name,
                "jaicf.request.client_id" to hook.request.clientId,
                "jaicf.session.new" to hook.requestContext.newSession,
                "jaicf.duration_ms" to hook.durationMs
            )
            val parentSpan = hook.context.getCurrentTelemetrySpan()
            val span = try {
                telemetryProvider.createSpan(name, attributes, parentSpan)
            } catch (e: Throwable) {
                TelemetrySpan.NoOp
            }
            if (span != TelemetrySpan.NoOp) {
                hook.context.setTelemetrySpan(name, span)
                hook.context.setCurrentTelemetrySpan(span)
            }
        } else {
            val span = hook.context.getTelemetrySpan("jaicf.request.start")
            span?.apply {
                setAttribute("jaicf.duration_ms", hook.durationMs)
                setAttribute("jaicf.current_state", hook.context.dialogContext.currentState)
                close()
            }
            hook.context.removeTelemetrySpan("jaicf.request.start")
            val parentSpan = hook.context.getTelemetrySpan("jaicf.bot.request")
            hook.context.setCurrentTelemetrySpan(parentSpan)
        }
    }

    fun handleBeforeProcess(telemetryProvider: TelemetryProvider, hook: BeforeProcessHook) {
        val activationSpan = hook.context.getTelemetrySpan("jaicf.activation.before")
        activationSpan?.close()
        hook.context.removeTelemetrySpan("jaicf.activation.before")

        val name = "jaicf.process.start"
        val attributes = mapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.request.input" to hook.request.input,
            "jaicf.activator" to hook.activator.javaClass.name,
            "jaicf.current_state" to hook.context.dialogContext.currentState
        )
        val parentSpan = hook.context.getTelemetrySpan("jaicf.bot.request")
            ?: hook.context.getCurrentTelemetrySpan()
        val span = try {
            telemetryProvider.createSpan(name, attributes, parentSpan)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
        if (span != TelemetrySpan.NoOp) {
            hook.context.setTelemetrySpan(name, span)
            hook.context.setCurrentTelemetrySpan(span)
        }
    }

    fun handleAfterProcess(telemetryProvider: TelemetryProvider, hook: AfterProcessHook) {
        val attributes = mapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.request.response" to hook.reactions.answer,
            "jaicf.activator" to hook.activator.javaClass.name,
            "jaicf.current_state" to hook.context.dialogContext.currentState
        )
        val span = hook.context.getTelemetrySpan("jaicf.process.start")
        span?.apply {
            attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
        }
        hook.context.removeTelemetrySpan("jaicf.process.start")
        val parentSpan = hook.context.getTelemetrySpan("jaicf.request.start")
        hook.context.setCurrentTelemetrySpan(parentSpan)
    }

    fun handleActionError(telemetryProvider: TelemetryProvider, hook: ActionErrorHook) {
        val name = "jaicf.action.error"
        val attributes = mutableMapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.state" to hook.state.path,
            "jaicf.state_path" to hook.state.path.toString(),
            "jaicf.activator" to hook.activator.javaClass.name,
            "jaicf.error.type" to hook.exception::class.qualifiedName,
            "jaicf.error.message" to hook.exception.message,
            "jaicf.error.state" to hook.state.path
        )
        telemetryProvider.record(name, attributes, hook.context)
        hook.context.getCurrentTelemetrySpan()?.recordException(hook.exception)
    }

    fun handleAnyError(telemetryProvider: TelemetryProvider, hook: AnyErrorHook) {
        val name = "jaicf.error"
        val attributes = mutableMapOf<String, Any?>(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.error.type" to hook.exception::class.qualifiedName,
            "jaicf.error.message" to hook.exception.message,
            "jaicf.current_state" to hook.context.dialogContext.currentState
        )
        telemetryProvider.record(name, attributes, hook.context)
        hook.context.getCurrentTelemetrySpan()?.recordException(hook.exception)
    }

    fun TelemetryProvider.record(
        name: String,
        attributes: Map<String, Any?>,
        context: BotContext? = null,
    ) {
        val parent = context?.getCurrentTelemetrySpan()
        val span = try {
            createSpan(name, attributes, parent)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
        span.use { span ->
            // Span is automatically closed by use block
            // No need to set it in context for short-lived spans
        }
    }
}

fun durationMillis(startedAt: Long): Double =
    (System.nanoTime() - startedAt) / 1_000_000.0
