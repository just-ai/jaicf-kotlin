package com.justai.jaicf.builder

import com.justai.jaicf.activator.catchall.CatchAllActivationRule
import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule
import com.justai.jaicf.activator.intent.IntentActivationRule
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.intent.IntentByNameActivationRule
import com.justai.jaicf.activator.regex.RegexActivationRule
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.activation.disableIf
import org.intellij.lang.annotations.Language

@ScenarioDsl
class ActivationRulesBuilder internal constructor(
    private val fromState: String,
    private val toState: String
) {

    private val rules = mutableListOf<ActivationRule>()

    /**
     * Registers the provided [ActivationRule].
     */
    fun <T: ActivationRule> rule(rule: T): T = rule.also(rules::add)

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

    /**
     * Allows activation of [this] rule only from a direct parent in a scenario tree.
     */
    fun ActivationRule.onlyFromParent() = onlyIf { context.dialogContext.currentContext == fromState }

    /**
     * Disable activation of [this] rule from direct and indirect children in a scenario tree.
     */
    fun ActivationRule.disableFromChildren() = disableIf {
        val current = context.dialogContext.currentContext
        current.startsWith(toState) && current.length != toState.length
    }
}