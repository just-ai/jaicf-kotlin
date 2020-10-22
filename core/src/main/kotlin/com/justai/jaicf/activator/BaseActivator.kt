package com.justai.jaicf.activator

import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition

/**
 * A base abstraction for every [Activator] that should activate a state if it contains a particular rule like event or intent.
 *
 * @see ActivationRule
 * @see com.justai.jaicf.activator.event.BaseEventActivator
 * @see com.justai.jaicf.activator.intent.BaseIntentActivator
 */
abstract class BaseActivator(private val model: ScenarioModel) : Activator {

    override fun activate(botContext: BotContext, request: BotRequest, selector: ActivationSelector): Activation? {
        val transitions = generateTransitions(botContext)
        val matcher = provideRuleMatcher(botContext, request)

        val activations = transitions.mapNotNull { transition ->
            matcher.match(transition.rule)?.let { transition to it }
        }

        return selector.selectActivation(botContext, activations)
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
        val isModal = currentState != "/" && model.states[currentState]?.modal
                ?: error("State $currentState is not registered in model")

        val currentPath = StatePath.parse(currentState)
        val availableStates = mutableListOf(currentPath.toString()).apply {
            if (isModal) addAll(currentPath.parents)
        }

        val transitionsMap = model.transitions.groupBy { it.fromState }
        return availableStates.flatMap { transitionsMap[it] ?: emptyList() }
    }
}