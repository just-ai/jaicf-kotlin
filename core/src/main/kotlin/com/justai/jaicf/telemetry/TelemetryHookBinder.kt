package com.justai.jaicf.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.hook.AfterActionHook
import com.justai.jaicf.hook.AfterProcessHook
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BeforeActionHook
import com.justai.jaicf.hook.BeforeActivationHook
import com.justai.jaicf.hook.BeforeProcessHook
import com.justai.jaicf.hook.RequestLifecycleStage
import com.justai.jaicf.hook.TelemetryStartProcessHook
import com.justai.jaicf.test.reactions.answer

/**
 * Wraps the request processing in a parent telemetry span.
 * Creates a span named "jaicf.bot.request" with request attributes and duration.
 */
suspend inline fun <T> BotEngine.runWithTelemetry(
    request: BotRequest,
    requestContext: RequestContext,
    context: BotContext,
    crossinline block: suspend () -> T
): T {
    val startedAt = System.nanoTime()
    val attributes = mapOf(
        "jaicf.request.type" to request.type.name,
        "jaicf.request.client_id" to request.clientId,
        "jaicf.session.new" to requestContext.newSession
    )

    val parentSpan = context.getCurrentTelemetrySpan()
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
            // Only clear current span if it's the same one
            if (context.getCurrentTelemetrySpan() == span) {
                context.setCurrentTelemetrySpan(null)
            }
        }
    }
}


internal fun BotEngine.installTelemetryHooks(telemetryProvider: TelemetryProvider = TelemetryProvider.NoOp) {

//    hooks.addHookAction<BotRequestHook> {
//        TelemetryHookProcessor.handleBotRequest(telemetryProvider, this)
//    }

    hooks.addHookAction<TelemetryStartProcessHook> {
        TelemetryHookProcessor.handleRequestLifecycle(telemetryProvider, this)
    }

    hooks.addHookAction<BeforeActivationHook> {
        TelemetryHookProcessor.handleBeforeActivation(telemetryProvider, this)
    }

    hooks.addHookAction<BeforeProcessHook> {
        TelemetryHookProcessor.handleBeforeProcess(telemetryProvider, this)
    }

    hooks.addHookAction<AfterProcessHook> {
        TelemetryHookProcessor.handleAfterProcess(telemetryProvider, this)
    }

    hooks.addHookAction<BeforeActionHook> {
        TelemetryHookProcessor.handleBeforeAction(telemetryProvider, this)
    }

    hooks.addHookAction<AfterActionHook> {
        TelemetryHookProcessor.handleAfterAction(telemetryProvider, this)
    }

    hooks.addHookAction<ActionErrorHook> {
        TelemetryHookProcessor.handleActionError(telemetryProvider, this)
    }

    hooks.addHookAction<AnyErrorHook> {
        TelemetryHookProcessor.handleAnyError(telemetryProvider, this)
    }
}

private object TelemetryHookProcessor {

//    fun handleBotRequest(telemetryProvider: TelemetryProvider, hook: BotRequestHook) {
//        val name = "jaicf.request.received"
//        val attributes = mapOf(
//            "jaicf.request.type" to hook.request.type.name,
//            "jaicf.request.client_id" to hook.request.clientId,
//            "jaicf.request.input" to hook.request.input
//        )
//        telemetryProvider.record(name, attributes)
//    }

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
            // Создаем долгоживущий span как дочерний от текущего (jaicf.bot.request)
            val parentSpan = hook.context.getCurrentTelemetrySpan()
            val span = try {
                telemetryProvider.createSpan(name, attributes, parentSpan)
            } catch (e: Throwable) {
                TelemetrySpan.NoOp
            }
            if (span != TelemetrySpan.NoOp) {
                hook.context.setTelemetrySpan(name, span)
                hook.context.setCurrentTelemetrySpan(span) // Устанавливаем как текущий!
            }
        } else {
            // END stage - обновляем и закрываем span
            val span = hook.context.getTelemetrySpan("jaicf.request.start")
            span?.apply {
                setAttribute("jaicf.duration_ms", hook.durationMs)
                setAttribute("jaicf.current_state", hook.context.dialogContext.currentState)
                close()
            }
            hook.context.removeTelemetrySpan("jaicf.request.start")
            // Восстанавливаем родительский span (jaicf.bot.request) как текущий
            val parentSpan = hook.context.getTelemetrySpan("jaicf.bot.request")
            hook.context.setCurrentTelemetrySpan(parentSpan)
        }
    }

    fun handleBeforeActivation(telemetryProvider: TelemetryProvider, hook: BeforeActivationHook) {
        val name = "jaicf.activation.before"
        val attributes = mapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.request.input" to hook.request.input,
            "jaicf.current_state" to hook.context.dialogContext.currentState
        )
        val parentSpan = hook.context.getTelemetrySpan("jaicf.bot.request")
        val span = try {
            telemetryProvider.createSpan(name, attributes, parentSpan)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
        if (span != TelemetrySpan.NoOp) {
            hook.context.setTelemetrySpan(name, span)
            hook.context.setCurrentTelemetrySpan(span) // Устанавливаем как текущий!
        }
    }

    fun handleBeforeProcess(telemetryProvider: TelemetryProvider, hook: BeforeProcessHook) {
        // Закрываем jaicf.activation.before перед началом процесса
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
        // Создаем долгоживущий span как дочерний от текущего (jaicf.request.start)
        val parentSpan = hook.context.getTelemetrySpan("jaicf.activation.before")
            ?: hook.context.getCurrentTelemetrySpan()
        val span = try {
            telemetryProvider.createSpan(name, attributes, parentSpan)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
        if (span != TelemetrySpan.NoOp) {
            hook.context.setTelemetrySpan(name, span)
            hook.context.setCurrentTelemetrySpan(span) // Устанавливаем как текущий!
        }
    }

    fun handleAfterProcess(telemetryProvider: TelemetryProvider, hook: AfterProcessHook) {
        val name = "jaicf.process.end"
        val attributes = mapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.request.response" to hook.reactions.answer,
            "jaicf.activator" to hook.activator.javaClass.name,
            "jaicf.current_state" to hook.context.dialogContext.currentState
        )
        // Обновляем и закрываем текущий span (jaicf.process.start)
        val span = hook.context.getTelemetrySpan("jaicf.process.start")
        span?.apply {
            attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
        }
        hook.context.removeTelemetrySpan("jaicf.process.start")
        // Восстанавливаем родительский span (jaicf.request.start) как текущий
        val parentSpan = hook.context.getTelemetrySpan("jaicf.request.start")
        hook.context.setCurrentTelemetrySpan(parentSpan)
    }

    fun handleBeforeAction(telemetryProvider: TelemetryProvider, hook: BeforeActionHook) {
        val name = "jaicf.action.start"
        val attributes = mapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.state" to hook.state.path,
            "jaicf.state_path" to hook.state.path.toString(),
            "jaicf.activator" to hook.activator.javaClass.name,
        )
        // Создаем долгоживущий span как дочерний от текущего (jaicf.process.start)
        val parentSpan = hook.context.getCurrentTelemetrySpan()
        val span = try {
            telemetryProvider.createSpan(name, attributes, parentSpan)
        } catch (e: Throwable) {
            TelemetrySpan.NoOp
        }
        if (span != TelemetrySpan.NoOp) {
            hook.context.setTelemetrySpan(name, span)
            hook.context.setCurrentTelemetrySpan(span) // Устанавливаем как текущий!
        }
    }

    fun handleAfterAction(telemetryProvider: TelemetryProvider, hook: AfterActionHook) {
        val name = "jaicf.action.end"
        val attributes = mapOf(
            "jaicf.request.client_id" to hook.request.clientId,
            "jaicf.response.answers" to hook.reactions.answer,
            "jaicf.state" to hook.state.path,
            "jaicf.state_path" to hook.state.path.toString(),
            "jaicf.activator" to hook.activator.javaClass.name,
        )
        // Обновляем и закрываем текущий span (jaicf.action.start)
        val span = hook.context.getTelemetrySpan("jaicf.action.start")
        span?.apply {
            attributes.forEach { (k, v) -> setAttribute(k, v) }
            close()
        }
        hook.context.removeTelemetrySpan("jaicf.action.start")
        // Восстанавливаем родительский span (jaicf.process.start) как текущий
        val parentSpan = hook.context.getTelemetrySpan("jaicf.process.start")
        hook.context.setCurrentTelemetrySpan(parentSpan)
    }

    fun handleActionError(telemetryProvider: TelemetryProvider, hook: ActionErrorHook) {
        val name = "jaicf.action.error"
        val attributes = mutableMapOf<String, Any?>(
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
