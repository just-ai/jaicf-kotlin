package com.justai.jaicf.activator.catchall

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
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
class CatchAllActivator(model: ScenarioModel) : BaseActivator(model) {

    override val name = "catchAllActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery() || request.hasIntent()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest) =
        ruleMatcher<CatchAllActivationRule> { CatchAllActivatorContext() }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = CatchAllActivator(model)
    }

}