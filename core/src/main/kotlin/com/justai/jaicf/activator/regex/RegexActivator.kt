package com.justai.jaicf.activator.regex

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRuleType
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.*

/**
 * This activator handles query requests and activates a state if it contains pattern that matches the request's input.
 * Produces [RegexActivatorContext]
 *
 * @param model dialogue scenario model
 */
class RegexActivator(model: ScenarioModel) : Activator {

    override val name = "regexActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    private val transitions = model.activations
        .filter { a -> a.type == ActivationRuleType.regexp }
        .map { a -> Pair(a.fromState, Pair(Pattern.compile(a.rule), a.toState)) }
        .groupBy {a -> a.first}
        .mapValues { l -> l.value.map { v -> v.second } }

    override fun activate(
        botContext: BotContext,
        request: BotRequest
    ): Activation? {
        val path = StatePath.parse(botContext.dialogContext.currentContext)
        return checkWithParents(path, request.input)
    }

    private fun checkWithParents(
        path: StatePath,
        query: String
    ): Activation? {
        var p = path
        while (true) {
            val res = check(p.toString(), query)
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

    private fun check(
        path: String,
        query: String
    ): Activation? {
        val rules = transitions[path]
        rules?.forEach { r ->
            var m = r.first.matcher(query)
            if (!m.matches()) {
                m = r.first.matcher(query.toLowerCase())
            }
            if (m.matches()) {
                val context = RegexActivatorContext(r.first)
                storeVariables(context, m)
                return Activation(r.second, context)
            }
        }
        return null
    }

    private fun storeVariables(context: RegexActivatorContext, m: Matcher) {
        for (i in 0 .. m.groupCount()) {
            context.groups.add(m.group(i))
        }

        for (e in getGroupNames(m.pattern())) {
            if (e.value < context.groups.size) {
                context.namedGroups[e.key] = context.groups[e.value]
            }
        }
    }

    private fun getGroupNames(p: Pattern): Map<String, Int> {
        val f = p.javaClass.getDeclaredField("namedGroups")
        f.isAccessible = true
        val v = f.get(p)
        return if (v != null) {
            @Suppress("UNCHECKED_CAST")
            val namedGroups = v as Map<String, Int>
            namedGroups
        } else {
            Collections.emptyMap()
        }
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel): Activator {
            return RegexActivator(model)
        }
    }

}