package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.channel.jaicp.dto.TelephonyBargeInRequest
import com.justai.jaicf.channel.jaicp.dto.TelephonyQueryRequest
import com.justai.jaicf.channel.jaicp.dto.bargeIn
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.exceptions.scenarioCause
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.hook.BeforeActivationHook
import com.justai.jaicf.hook.BeforeProcessHook
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.model.state.StatePath


/**
 * Customizable bargeInIntent event processor enabling speech synthesis interruption mechanism.
 *
 * The logic behind this processor is:
 *  1. Once [TelephonyBargeInRequest] is received, a [handleBeforeActivation] is invoked to set dialog context to find transitions in.
 *  2. [handleBeforeActivation] mutates current dialog context and stores a number of keys in context.
 *  3. [handleBeforeProcess] checks keys to resolve if we're inside barge-in transition. It blocks this transition.
 *  4. [handleBeforeProcess] calls [isAllowInterruption] to resolve if we need to interrupt. The [TelephonyBargeInRequest] is finished.
 *  5. After we responded and allowed interruption, next [TelephonyQueryRequest] will contain request which we should process.
 *  6. After next [TelephonyQueryRequest] is received, we set stored dialog context and clean session keys.
 * */
open class BargeInProcessor : WithLogger {

    companion object {
        protected const val TEMP_ACTIVATION_KEY = "isBargeInActivation"
        protected const val ORIGINAL_CONTEXT_KEY =
            "com/justai/jaicf/channel/jaicp/bargein/bargeInProcessor/originalContext"
        protected const val BARGEIN_CONTEXT_KEY =
            "com/justai/jaicf/channel/jaicp/bargein/bargeInProcessor/bargeInContext"
        val NON_FALLBACK = object : BargeInProcessor() {
            override fun isAllowInterruption(hook: BeforeProcessHook) =
                hook.context.dialogContext.nextState?.endsWith("/fallback") != true
        }
    }

    /**
     * Appended as hook to scenario in TelephonyChannel to process bargeIn events.
     * Mutates [DialogContext] to select states from another context with bargeIn speech synthesis interruption.
     *
     * @see DialogContext
     * @see TelephonyBargeInRequest
     * */
    open fun handleBeforeActivation(hook: BeforeActivationHook) {
        hook.request.bargeIn?.run {
            // stores current context to restore it if something's gone wrong on activation
            hook.context.session[ORIGINAL_CONTEXT_KEY] = hook.context.dialogContext.currentContext

            val current = StatePath.parse(hook.context.dialogContext.currentContext)
            val resolved = current.resolve(transition)
            if (current != resolved) {
                hook.context.dialogContext.currentContext = resolved.toString()
            }
            hook.context.session[BARGEIN_CONTEXT_KEY] = resolved.toString()
            hook.context.temp[TEMP_ACTIVATION_KEY] = true
        } ?: run {
            val bargedInContext = hook.context.session[BARGEIN_CONTEXT_KEY] as? String
            if (bargedInContext != null) {
                hook.context.dialogContext.currentContext = bargedInContext
                hook.context.session.remove(ORIGINAL_CONTEXT_KEY)
            }
        }

        logger.debug("Exit beforeActivationHook with dialogContext")
        logger.debug("Current context: ${hook.context.dialogContext.currentContext}")
    }

    /**
     * Appended as hook to scenario in TelephonyChannel to process bargeInIntent events.
     * Allows (or declines) speech synthesis interruption for [TelephonyBargeInRequest] by calling [isAllowInterruption],
     *  forbids states, selected by [TelephonyBargeInRequest], active in scenario.
     *
     * @see DialogContext
     * @see TelephonyBargeInRequest
     * */
    open fun handleBeforeProcess(hook: BeforeProcessHook) {
        if (hook.context.temp[TEMP_ACTIVATION_KEY] == true) {
            if (isAllowInterruption(hook)) {
                hook.reactions.telephony?.allowInterrupt()
            } else {
                hook.restoreContextAndExit()
            }

            logger.trace("Skip processing states by bargeIn activation")
            hook.context.dialogContext.nextState = null
            hook.context.dialogContext.nextContext = null
            hook.context.dialogContext.nextContext = null
        }
    }

    /**
     * Appended as hook to scenario in TelephonyChannel to process bargeIn events.
     * Handles error in activation and restores dialogContext after unsuccessful barge-in attempt.
     *
     * @see DialogContext
     * @see TelephonyBargeInRequest
     * */
    open fun handleActivationError(hook: AnyErrorHook) {
        if (hook.context.temp[TEMP_ACTIVATION_KEY] == true) {
            logger.debug("Handling barge-in hook error: ", hook.exception.scenarioCause)
            hook.restoreContextAndExit()
        } else {
            throw hook.exception.scenarioCause
        }
    }

    /**
     * Resolves if bargeIn should interrupt synthesis or playback.
     *
     * @param hook is [BeforeProcessHook] containing request processing information
     *
     * @return true if interruption is allowed.
     * */
    open fun isAllowInterruption(hook: BeforeProcessHook) = true

    private fun BotHook.restoreContextAndExit() {
        context.dialogContext.currentContext = context.session[ORIGINAL_CONTEXT_KEY] as String
        context.session.remove(BARGEIN_CONTEXT_KEY)
    }
}
