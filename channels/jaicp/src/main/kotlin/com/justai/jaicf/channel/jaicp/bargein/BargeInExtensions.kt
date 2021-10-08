package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.jaicp.dto.TelephonyBargeInRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.activation.disableIf


/**
 * Allows [this] rule only for barge-in requests
 *
 * @see ActivationRule.onlyIf
 * @see BargeInProcessor
 * @see TelephonyBargeInRequest
 */
fun ActivationRule.onlyIfBargeIn() = onlyIf { isBargeIn(context, request) }

/**
 * Disable [this] rule for barge-in requests
 *
 * @see ActivationRule.onlyIf
 * @see ActivationRule.disableIf
 * @see BargeInProcessor
 * @see TelephonyBargeInRequest
 */
fun ActivationRule.disableIfBargeIn() = disableIf { isBargeIn(context, request) }

/**
 * Checks whether the current request is barge-in request
 */
private fun isBargeIn(botContext: BotContext, request: BotRequest): Boolean {
    val bargeInProcessor = botContext.temp[BargeInProcessor.BARGE_IN_PROCESSOR_TEMP_KEY] as? BargeInProcessor
        ?: return false
    return request is TelephonyBargeInRequest || bargeInProcessor.isAfterSuccessfullBargeIn(botContext, request)
}