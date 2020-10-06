package com.justai.jaicf.activator.regex

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel
import java.util.regex.Pattern

/**
 * This activator handles query requests and activates a state if it contains pattern that matches the request's input.
 * Produces [RegexActivatorContext]
 *
 * @param model dialogue scenario model
 */
class RegexActivator(model: ScenarioModel) : BaseActivator(model) {

    override val name = "regexActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest) =
        ruleMatcher<RegexActivationRule> { rule ->
            val pattern = Pattern.compile(rule.regex, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
            val matcher = pattern.matcher(request.input)
            if (matcher.matches()) {
                RegexActivatorContext(pattern, matcher)
            } else {
                null
            }
        }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = RegexActivator(model)
    }
}