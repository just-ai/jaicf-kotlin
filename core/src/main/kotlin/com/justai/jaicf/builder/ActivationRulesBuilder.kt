package com.justai.jaicf.builder

import com.justai.jaicf.activator.catchall.CatchAllActivationRule
import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule
import com.justai.jaicf.activator.intent.IntentByNameActivationRule
import com.justai.jaicf.activator.regex.RegexActivationRule
import com.justai.jaicf.model.activation.ActivationRule
import org.intellij.lang.annotations.Language

@ScenarioDsl
class ActivationRulesBuilder internal constructor() {
    private val rules = mutableListOf<ActivationRule>()

    /**
     * Registers the provided [ActivationRule].
     */
    fun rule(rule: ActivationRule) {
        rules += rule
    }

    internal fun build(): List<ActivationRule> = rules

    /**
     * Registers catch-all activation rule that handles any request.
     * Requires a [com.justai.jaicf.activator.catchall.CatchAllActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.catchall.CatchAllActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun catchAll() = rule(CatchAllActivationRule())

    /**
     * Registers regex activation rule that handles any text that matches the [pattern].
     * Requires a [com.justai.jaicf.activator.regex.RegexActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.regex.RegexActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun regex(pattern: Regex) = rule(RegexActivationRule(pattern.pattern))

    /**
     * Registers regex activation rule that handles any text that matches the [pattern].
     * Requires a [com.justai.jaicf.activator.regex.RegexActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.regex.RegexActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun regex(@Language("RegExp") pattern: String) = regex(pattern.toRegex())

    /**
     * Registers event activation rule that handles an event with name [event].
     * Requires a [com.justai.jaicf.activator.event.EventActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.event.EventActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun event(event: String) = rule(EventByNameActivationRule(event))

    /**
     * Registers any-event activation rule that handles any event.
     * Requires a [com.justai.jaicf.activator.event.EventActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.event.EventActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun anyEvent() = rule(AnyEventActivationRule())

    /**
     * Registers intent activation rule that handles an intent with name [intent].
     * Requires a [com.justai.jaicf.activator.intent.IntentActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.intent.IntentActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun intent(intent: String) = rule(IntentByNameActivationRule(intent))

    /**
     * Registers any-intent activation rule that handles any intent.
     * Requires a [com.justai.jaicf.activator.intent.IntentActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
     *
     * @see com.justai.jaicf.activator.intent.IntentActivator
     * @see com.justai.jaicf.api.BotApi
     */
    fun anyIntent() = rule(AnyIntentActivationRule())

}