package com.justai.jaicf.activator

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath
import com.sun.org.apache.xpath.internal.operations.Bool

/**
 * A helper abstraction for every [Activator] that should activate a state if it contains a particular rule like event or intent.
 *
 * @see com.justai.jaicf.activator.event.BaseEventActivator
 * @see com.justai.jaicf.activator.intent.BaseIntentActivator
 */
abstract class StateMapActivator(model: ScenarioModel): Activator {

    private val transitions = model.transitions
        .filter { t -> canHandleRule(t.rule) }
        .map { a -> Pair(a.fromState, Pair(a.rule, a.toState)) }
        .groupBy {a -> a.first}
        .mapValues { l -> l.value.map { v -> v.second } }

    abstract fun canHandleRule(rule: ActivationRule): Boolean

    fun findState(botContext: BotContext, predicate: (ActivationRule) -> Boolean): String? {
        val path = StatePath.parse(botContext.dialogContext.currentContext)
        return checkWithParents(path, predicate)
    }

    private fun checkWithParents(path: StatePath, predicate: (ActivationRule) -> Boolean): String? {
        var p = path
        while (true) {
            val res = findState(p.toString(), predicate)
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

    private fun findState(path: String, predicate: (ActivationRule) -> Boolean): String? =
        transitions[path]?.firstOrNull { predicate(it.first) }?.second
}