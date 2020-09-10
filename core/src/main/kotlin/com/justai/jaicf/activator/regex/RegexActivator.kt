package com.justai.jaicf.activator.regex

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.StateMapActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This activator handles query requests and activates a state if it contains pattern that matches the request's input.
 * Produces [RegexActivatorContext]
 *
 * @param model dialogue scenario model
 */
class RegexActivator(model: ScenarioModel) : StateMapActivator(model) {

    override val name = "regexActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun canMatchRule(rule: ActivationRule) = rule is RegexActivationRule

    override fun provideActivationRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher? {
        return object : ActivationRuleMatcher {
            override fun match(rule: ActivationRule): ActivatorContext? {
                val regex = (rule as? RegexActivationRule)?.regex ?: return null
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
                val matcher = pattern.matcher(request.input)
                return if (matcher.matches()) {
                    RegexActivatorContext(pattern).also { storeVariables(it, matcher) }
                } else {
                    null
                }
            }
        }
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