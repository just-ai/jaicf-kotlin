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
    /**
     * Process a given [BotRequest] aware of a current [BotContext] and returns all intents that were recognized
     * by this [IntentActivator] in the request.
     *
     * This function will be called once on single [BotRequest].
     *
     * @param botContext a current [BotContext]
     * @param request a current user's [BotRequest]
     * @return list of recognized intents represented as [IntentActivatorContext]
     *
     * @see IntentActivatorContext
     * @see BaseIntentActivator
     */
    fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext>
}