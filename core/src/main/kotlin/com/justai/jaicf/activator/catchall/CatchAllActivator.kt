package com.justai.jaicf.activator.catchall

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRuleType
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath

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
class CatchAllActivator(model: ScenarioModel) : Activator {

    override fun canHandle(request: BotRequest) = request.hasQuery() || request.hasIntent()

    private val transitions = model.activations
        .filter { a -> a.type == ActivationRuleType.anytext }
        .associate { a -> Pair(a.fromState, a.toState) }

    override fun activate(
        botContext: BotContext,
        request: BotRequest
    ): Activation? {
        val path = StatePath.parse(botContext.dialogContext.currentContext)
        val state = checkWithParents(path)

        return state?.let {
            Activation(it, CatchAllActivatorContext())
        }
    }

    private fun checkWithParents(path: StatePath): String? {
        var p = path
        while (true) {
            val res = check(p.toString())
            if (res != null) {
                return res
            }
            if (p.toString() == "/") {
                break
            }
            p = p.stepUp()
        }
        return null
    }

    private fun check(path: String): String? {
        return transitions[path]
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return CatchAllActivator(model)
        }
    }

}