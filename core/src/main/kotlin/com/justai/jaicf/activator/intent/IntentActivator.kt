package com.justai.jaicf.activator.intent

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext

/**
 * A base interface for intent activators.
 * This type of activators handle intent requests and activates a state if it contains this intent name.
 * Intents may be recognised by NLU engines like Caila, Dialogflow, Alexa and others.
 * Produces [IntentActivatorContext] instance.
 *
 * Usage example:
 *
 * ```
 * state("some state") {
 *   activators {
 *     intent("WelcomeIntent")
 *   }
 *
 *   action {
 *     reactions.say("Welcome to the awesome skill!")
 *   }
 * }
 * ```
 *
 * @see BaseIntentActivator
 */
interface IntentActivator: Activator {
    fun recogniseIntent(botContext: BotContext, request: BotRequest): IntentActivatorContext?
}