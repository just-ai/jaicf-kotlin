package com.justai.jaicf.channel.max.dto

import com.justai.jaicf.channel.max.exception.MaxApiException
import com.justai.jaicf.channel.max.exception.MaxBotBlockedException
import com.justai.jaicf.channel.max.exception.MaxInvalidTokenException
import com.justai.jaicf.channel.max.exception.MaxRateLimitException

/** Error body returned by the Max Bot API. */
internal data class MaxApiError(val code: String? = null, val message: String? = null)

/** Map an HTTP status (+ optional parsed error body) to a typed [MaxApiException]. */
internal fun MaxApiError?.toException(httpStatus: Int): MaxApiException {
    val code = this?.code
    val message = this?.message
    return when (httpStatus) {
        401 -> MaxInvalidTokenException(httpStatus, code, message)
        403 -> MaxBotBlockedException(httpStatus, code, message)
        429 -> MaxRateLimitException(httpStatus, code, message)
        else -> MaxApiException(httpStatus, code, message)
    }
}
