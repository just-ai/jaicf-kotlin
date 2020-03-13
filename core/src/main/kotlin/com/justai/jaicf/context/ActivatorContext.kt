package com.justai.jaicf.context

/**
 * Main abstraction for [com.justai.jaicf.activator.Activator] context.
 * A particular implementation should contain some activator-related properties that could be used in scenario's action block.
 * As a rule contains confidence of recognised intent, named entities fetched from the request and other NLU-related details.
 *
 * @property confidence a confidence of recognised user's intent in range between 0 and 1. Indicates the level of activator's confidence.
 *
 * @see com.justai.jaicf.activator.Activator
 */
interface ActivatorContext {
    val confidence: Float
}

/**
 * Strict confident activator context abstraction.
 * Subclasses represent contexts of activators that cannot measure the confidence in between 0 and 1.
 *
 * @see com.justai.jaicf.activator.catchall.CatchAllActivatorContext
 * @see com.justai.jaicf.activator.event.EventActivatorContext
 * @see com.justai.jaicf.activator.regex.RegexActivatorContext
 */
open class StrictActivatorContext: ActivatorContext {
    override val confidence = 1f
}