package com.justai.jaicf.activator

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.activation.ActivationSelector
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.state.StatesTransition
import com.justai.jaicf.model.transition.Transition

/**
 * A base abstraction for every [Activator] that should activate a state if it contains a particular rule like event or intent.
 *
 * @see ActivationRule
 * @see com.justai.jaicf.activator.event.BaseEventActivator
 * @see com.justai.jaicf.activator.intent.BaseIntentActivator
 * @see com.justai.jaicf.model.activation.ActivationSelector
 */
abstract class BaseActivator(model: ScenarioModel) : Activator {

    private val transitions = model.transitions.groupBy { it.fromState }

    override fun activate(
        botContext: BotContext,
        request: BotRequest,
        activationSelector: ActivationSelector
    ): Activation? {
        val matcher = provideRuleMatcher(botContext, request)
        val transitions = generateTransitions(botContext)

        val activations = transitions.mapNotNull { transition ->
            matcher.match(transition.rule)?.let {
                Activation(StatesTransition(transition.fromState, transition.toState), it)
            }
        }.toList()

        if (activations.isEmpty()) return null
        return activationSelector.selectActivation(botContext, activations)
    }

    /**
     * Every custom [BaseActivator] implementation must provide [ActivationRuleMatcher] object on each [BotRequest].
     *
     * This method will be called once on each [BotRequest], and [ActivationRuleMatcher.match] method of a provided
     * matcher will be called multiple times with different activation rules, so you should perform all stateful
     * processing related to single request in this method once and provide then stateless [ActivationRuleMatcher].
     *
     * @param botContext a current user's [BotContext]
     * @param request a current request
     * @return [ActivationRuleMatcher] to determine transitions that can be made from current context by this request.
     *
     * @see ActivationRuleMatcher
     * @see BaseActivator.activate
     */
    protected abstract fun provideRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher

    /**
     * Helper method for building an [ActivationRuleMatcher] that can only match rules of a certain type.
     *
     * [ActivationRuleMatcher.match] of provided matcher will return `null` in case of a rule that is not of type [R],
     * otherwise [matcher] will be called on rule casted to [R].
     *
     * @see ActivationRuleMatcher
     */
    protected inline fun <reified R : ActivationRule> ruleMatcher(crossinline matcher: (R) -> ActivatorContext?) =
        object : ActivationRuleMatcher {
            override fun match(rule: ActivationRule) = (rule as? R)?.let(matcher)
        }

    private fun generateTransitions(botContext: BotContext): Sequence<Transition> {
        val currentState = StatePath.parse(botContext.dialogContext.currentContext).resolve(".")
        val states = generateSequence(currentState) { if (it.toString() == "/") null else it.stepUp() }
        return states.flatMap { transitions[it.toString()]?.asSequence() ?: emptySequence() }
    }
}