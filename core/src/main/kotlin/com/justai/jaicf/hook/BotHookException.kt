package com.justai.jaicf.hook

import java.lang.Exception

/**
 * This exception should be used when you're willing to interrupt the request processing.
 * Just throw this exception form the body of corresponding [BotHook] listener.
 *
 * Usage example:
 *
 * ```
 * object HelloWorldScenario: Scenario() {
 *   init {
 *     handle<BotRequestHook> {
 *       throw BotHookException("I'm on vacation and won't process any request!")
 *     }
 *   }
 * }
 * ```
 */
class BotHookException(message: String? = null): Exception(message)