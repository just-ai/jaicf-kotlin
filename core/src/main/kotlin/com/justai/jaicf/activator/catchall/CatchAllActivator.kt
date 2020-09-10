package com.justai.jaicf.activator.catchall

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.StateMapActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * This activator handles query and intent requests and activates a state if request contains any input.
 * Thus this activator handles everything except events.
 * Produces [CatchAllActivatorContext] instance.
 *
 * Usage example:
 *
 * ```
 * state("fallback", noContext = true) {
 *   activators {
 *     catchAll()
 *   }
 *
 *   action {
 *     reactions.say("Sorry, I didn't get it. Could you repeat please?")
 *   }
 * }
 * ```
 *
 * @see CatchAllActivatorContext
 */
class CatchAllActivator(model: ScenarioModel) : StateMapActivator(model) {

    override val name = "catchAllActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery() || request.hasIntent()

    override fun canMatchRule(rule: ActivationRule) = rule is CatchAllActivationRule

    override fun provideActivationRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher? {
        return object : ActivationRuleMatcher {
            override fun match(rule: ActivationRule): CatchAllActivatorContext? {
                return (rule as? CatchAllActivationRule)?.let { CatchAllActivatorContext() }
            }
        }
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return CatchAllActivator(model)
        }
    }

}