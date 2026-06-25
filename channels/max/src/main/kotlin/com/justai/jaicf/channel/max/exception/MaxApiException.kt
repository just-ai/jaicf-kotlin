package com.justai.jaicf.channel.max.exception

/** Base for all Max Bot API errors. [code] is the API error code (e.g. "verify.token"); [httpStatus] the HTTP status. */
open class MaxApiException(
    val httpStatus: Int,
    val code: String? = null,
    message: String? = null,
) : RuntimeException(message ?: code ?: "Max API error (HTTP $httpStatus)")

/** Bot has no access to the chat — best-effort mapping of HTTP 403 (Max does not document a dedicated blocked code; confirm on the test stand). */
class MaxBotBlockedException(httpStatus: Int, code: String?, message: String?) : MaxApiException(httpStatus, code, message)

/**
 * HTTP 429 — the bot hit Max's rate limit. Not retried automatically: in the webhook path the update
 * is dropped after logging. Backoff-retry for 429 is a follow-up, out of this MVP's scope.
 */
class MaxRateLimitException(httpStatus: Int, code: String?, message: String?) : MaxApiException(httpStatus, code, message)

/** HTTP 401 / code "verify.token" — invalid or expired access token. */
class MaxInvalidTokenException(httpStatus: Int, code: String?, message: String?) : MaxApiException(httpStatus, code, message)
