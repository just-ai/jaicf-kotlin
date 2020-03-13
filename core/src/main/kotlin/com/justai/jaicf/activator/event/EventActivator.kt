package com.justai.jaicf.activator.event

import com.justai.jaicf.activator.Activator

/**
 * A base interface for event activators.
 * This type of activators handles event requests and activates a state if it contains this event name.
 * Produces [EventActivatorContext] instance.
 *
 * Usage example:
 *
 * ```
 * state("some state") {
 *   activators {
 *     event(AlexaEvent.LAUNCH)
 *   }
 *
 *   action {
 *     reactions.say("Welcome to the awesome voice skill!")
 *   }
 * }
 * ```
 *
 * @see BaseEventActivator
 */
interface EventActivator: Activator