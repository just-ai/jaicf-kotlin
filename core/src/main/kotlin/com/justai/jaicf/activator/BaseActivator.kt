package com.justai.jaicf.activator

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.transition.Transition

/**
 * A base abstraction for every [Activator] that should activate a state if it contains a particular rule like event or intent.
 *
 * @see ActivationRule
 * @see com.justai.jaicf.activator.event.BaseEventActivator
 * @see com.justai.jaicf.activator.intent.BaseIntentActivator
 */
abstract class BaseActivator(private val model: ScenarioModel) : Activator {

    override fun activate(botContext: BotContext, request: BotRequest): Activation? {
        val transitions = generateTransitions(botContext)
        val matcher = provideRuleMatcher(botContext, request)

        val activations = transitions.mapNotNull { transition ->
            matcher.match(transition.rule)?.let { transition to it }
        }

        return selectActivation(botContext, activations)
    }

    /**
     * This method is used for selection the most relevant activation.
     *
     * Default implementation at first tries to select an activation with the greatest confidence from all children
     * of the current state, then from all siblings (including current state),
     * then from all siblings of the parent (including the parent), and so on.
     *
     * @param botContext a current user's [BotContext]
     * @param activations list of all available activations
     * @return the most relevant [Activation] in terms of certain implementation of [BaseActivator]
     *
     * @see Activation
     */
    protected open fun selectActivation(
        botContext: BotContext,
        activations: List<Pair<Transition, ActivatorContext>>
    ): Activation? {
        val current = botContext.dialogContext.currentContext

        val toChildren = activations.filter { it.first.fromState == current }.maxBy { it.second.confidence }
        val toCurrent = activations.filter { it.first.toState == current }.maxBy { it.second.confidence }
        val fromRoot = activations.filter { it.first.fromState == "/" }.maxBy { it.second.confidence }

        val best = toChildren ?: toCurrent ?: fromRoot
        return best?.let { Activation(it.first.toState, it.second) }
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

    private fun generateTransitions(botContext: BotContext): List<Transition> {
        val currentState = botContext.dialogContext.currentContext
        return model.transitions.filter {
            it.fromState == currentState || it.toState == currentState || it.fromState == "/"
        }.distinct()
    }
}