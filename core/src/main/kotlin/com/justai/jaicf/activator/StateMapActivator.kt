package com.justai.jaicf.activator

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.ActivationRuleType
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath

/**
 * A helper abstraction for every [Activator] that should activate a state if it contains a particular rule like event or intent.
 *
 * @see com.justai.jaicf.activator.event.BaseEventActivator
 * @see com.justai.jaicf.activator.intent.BaseIntentActivator
 */
abstract class StateMapActivator(
    ruleType: ActivationRuleType,
    model: ScenarioModel
): Activator {

    private val transitions = model.activations
        .filter { a -> a.type == ruleType }
        .map { a -> Pair(a.fromState, Pair(a.rule, a.toState)) }
        .groupBy {a -> a.first}
        .mapValues { l -> l.value.map { v -> v.second } }

    fun findState(rule: String?, botContext: BotContext): String? = rule?.let {
        val path = StatePath.parse(botContext.dialogContext.currentContext)
        return checkWithParents(path, rule)
    }

    private fun checkWithParents(path: StatePath, rule: String): String? {
        var p = path
        while (true) {
            val res = findState(p.toString(), rule)
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

    private fun findState(path: String, rule: String): String? =
        transitions[path]?.firstOrNull { it.first == rule }?.second
}