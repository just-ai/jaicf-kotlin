package com.justai.jaicf.builder

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ActivatorTypeToken
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken
import com.justai.jaicf.model.ActionAdapter
import com.justai.jaicf.model.ScenarioModelBuilder
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition
import com.justai.jaicf.reactions.Reactions

@ScenarioDsl
sealed class ScenarioGraphBuilder<B : BotRequest, R : Reactions>(
    @Suppress("EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR")
    protected val scenarioModelBuilder: ScenarioModelBuilder,
    protected val channelToken: ChannelTypeToken<B, R>,
    protected val path: StatePath,
    protected val noContext: Boolean,
    protected val modal: Boolean
) {

    /**
     * Appends an inner state to the current state.
     *
     * @param name a name of the state. Could be plain text or contains slashes to define a state path
     * @param noContext indicates if this state should not to change the current dialogue's context
     * @param modal indicates if this state should process the user's request in modal mode ignoring all other states
     * @param body a code block of the state that contains activators, action and inner states definitions
     */
    @ScenarioDsl
    fun state(
        name: String,
        noContext: Boolean = false,
        modal: Boolean = false,
        body: StateBuilder<B, R>.() -> Unit
    ) {
        val state = StateBuilder(scenarioModelBuilder, channelToken, path, name, noContext, modal)
            .apply(body).build()
        scenarioModelBuilder.states.add(state)
    }

    /**
     * Appends a fallback state to the scenario.
     * This state will be activated for every request that doesn't match to any other state.
     * The current dialogue's context won't be changed.
     * This builder requires a CatchAllActivator to be added to the activators list of your BotEngine instance.
     *
     * ```
     * fallback {
     *   reactions.say("Sorry, I didn't get it...")
     * }
     * ```
     *
     * @param state an optional state name ("fallback" by default)
     * @param body an action block that will be executed
     */
    @ScenarioDsl
    fun fallback(
        name: String = "fallback",
        body: ActionContext<ActivatorContext, B, R>.() -> Unit
    ) = state(name, noContext = true) {
        activators { catchAll() }
        action(body)
    }

    /**
     * A channel-specific variation of [fallback].
     *
     * ```
     * fallback(telegram) {
     *   reactions.say("Sorry, ${request.message.chat.firstName}, I didn't get it.")
     * }
     * ```
     *
     * @param state an optional state name ("fallback" by default)
     * @param channelToken a type token of the channel
     * @param body an action block that will be executed only if request matches given [channelToken]
     */
    @ScenarioDsl
    fun <B1 : B, R1 : R> fallback(
        channelToken: ChannelTypeToken<B1, R1>,
        name: String = "fallback",
        body: ActionContext<ActivatorContext, B1, R1>.() -> Unit
    ) = fallback(name) { channelToken.invoke(body) }

    internal open fun build(): State = State(path, noContext, modal)
}

open class RootBuilder<B : BotRequest, R : Reactions> internal constructor(
    scenarioModelBuilder: ScenarioModelBuilder, channelToken: ChannelTypeToken<B, R>
) : ScenarioGraphBuilder<B, R>(scenarioModelBuilder, channelToken, StatePath.root(), false, false)

class StateBuilder<B : BotRequest, R : Reactions> internal constructor(
    scenarioModelBuilder: ScenarioModelBuilder,
    channelToken: ChannelTypeToken<B, R>,
    private val parent: StatePath,
    private val name: String,
    noContext: Boolean,
    modal: Boolean
) : ScenarioGraphBuilder<B, R>(scenarioModelBuilder, channelToken, parent.resolve(name), noContext, modal) {
    private var action: (ActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit)? = null

    /**
     * Appends activators to this state. Means that this state can be activated from [fromState] by the rules specified.
     * If the state is on top of the states hierarchy then these activators become global for scenario.
     *
     * @param fromState an optional state from where this state could be activated. If not specified the parent's state is used.
     * @param body a code block that contains activators list
     * @see com.justai.jaicf.activator.Activator
     */
    fun activators(fromState: String = parent.toString(), body: ActivationRulesBuilder.() -> Unit) {
        val rules = ActivationRulesBuilder().apply(body).build()
        val transitions = rules.map { Transition(fromState, path.toString(), it) }
        scenarioModelBuilder.transitions.addAll(transitions)
    }

    /**
     * Appends global activators for this state. Means that this state can be activated from any point of scenario.
     *
     * @param body a code block that contains activators list
     * @see com.justai.jaicf.activator.Activator
     */
    fun globalActivators(body: ActivationRulesBuilder.() -> Unit) = activators(StatePath.root().toString(), body)

    /**
     * An action that should be executed once this state was activated.
     * @param body a code block of the action
     */
    fun action(body: @ScenarioDsl ActionContext<ActivatorContext, B, R>.() -> Unit) {
        action = { channelToken.invoke(body) }
    }

    /**
     * An action that should be executed once this state was activated.
     * The action will be executed only if [ActionContext] type matches the given [activatorToken]
     *
     * @param activatorToken an activator type token
     * @param body a code block of the action
     */
    fun <A1 : ActivatorContext> action(
        activatorToken: ActivatorTypeToken<A1>,
        body: @ScenarioDsl ActionContext<A1, B, R>.() -> Unit
    ) = action { activatorToken.invoke(body) }

    /**
     * An action that should be executed once this state was activated.
     * The action will be executed only if [ActionContext] type matches the given [channelToken]
     *
     * @param channelToken a channel type token
     * @param body a code block of the action
     */
    fun <B1 : B, R1 : R> action(
        channelToken: ChannelTypeToken<B1, R1>,
        body: @ScenarioDsl ActionContext<ActivatorContext, B1, R1>.() -> Unit
    ) = action { channelToken.invoke(body) }

    /**
     * An action that should be executed once this state was activated.
     * The action will be executed only if [ActionContext] type matches the given [contextTypeToken]
     *
     * @param contextTypeToken a full context type token
     * @param body a code block of the action
     */
    fun <A1 : ActivatorContext, B1 : B, R1 : R> action(
        contextToken: ContextTypeToken<A1, B1, R1>,
        body: @ScenarioDsl ActionContext<A1, B1, R1>.() -> Unit
    ) = action { contextToken.invoke(body) }

    override internal fun build(): State = State(path, noContext, modal, action?.let(::ActionAdapter))
}