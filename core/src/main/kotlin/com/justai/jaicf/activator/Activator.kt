package com.justai.jaicf.activator

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFiller
import com.justai.jaicf.slotfilling.SlotFillingResult
import com.justai.jaicf.slotfilling.SlotFillingSkipped

/**
 * Main abstraction for state activator.
 * Every activator tries to handle the request and return a corresponding state of scenario that relates to the user's request.
 * It also returns an [com.justai.jaicf.context.ActivatorContext] instance that contains all activator-related properties (like recognised named entities) that is available in the action block of scenario through the [com.justai.jaicf.context.ActionContext].
 *
 * This abstraction is used by different NLU engine implementations.
 * There are some built-in activators available in SDK: CatchAllActivator, RegexActivator, BaseEventActivator and BaseIntentActivator.
 * SDK modules also contains a third-party activators like CailaActivator, DialogflowActivator, RasaActivator, AlexaActivator and others.
 *
 * @see com.justai.jaicf.context.ActivatorContext
 * @see com.justai.jaicf.context.ActionContext
 * @see com.justai.jaicf.activator.catchall.CatchAllActivator
 * @see com.justai.jaicf.activator.event.EventActivator
 * @see com.justai.jaicf.activator.regex.RegexActivator
 * @see com.justai.jaicf.activator.intent.IntentActivator
 */
interface Activator {

    /**
     * Signals if this activator can handle a particular [BotRequest].
     * Note that this doesn't mean that this activator actually should activate some state of scenario. It just indicates that this activator can work with such a request at all.
     * As a rule just looks on the type of request or it's class.
     *
     * @param request current user's request
     * @return true if this activator can handle such a request
     * @see BotRequest
     */
    fun canHandle(request: BotRequest): Boolean

    /**
     * Tries to handle user's request and find a state of the scenario related to this request.
     *
     * @param botContext a current user's [BotContext]
     * @param request a current [BotRequest]
     * @return [Activation] that contains an optional state of scenario and [com.justai.jaicf.context.ActivatorContext] or null if activator cannot handle a request at all.
     *
     * @see BotContext
     * @see BotRequest
     * @see com.justai.jaicf.context.ActivatorContext
     */
    fun activate(
        botContext: BotContext,
        request: BotRequest
    ): Activation?

    fun fillSlots(
        botContext: BotContext,
        request: BotRequest,
        reactions: Reactions,
        activatorContext: ActivatorContext?,
        slotFiller: SlotFiller? = null
    ): SlotFillingResult = SlotFillingSkipped
}

/**
 * A main abstraction for [Activator] factory.
 * Should be implemented by every activator to enable the user of a SDK to use this activator.
 * @see Activator
 */
interface ActivatorFactory {
    /**
     * Creates a new [Activator] instance using a passed scenario model.
     * @param model [ScenarioModel] that should be used by this [Activator] to find a state that corresponds to the request.
     * @return an [Activator] instance
     *
     * @see Activator
     * @see ScenarioModel
     */
    fun create(model: ScenarioModel): Activator
}

