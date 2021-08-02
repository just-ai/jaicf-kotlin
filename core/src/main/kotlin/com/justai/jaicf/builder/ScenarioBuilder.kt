package com.justai.jaicf.builder

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ActivatorTypeToken
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookListener
import com.justai.jaicf.model.ActionAdapter
import com.justai.jaicf.model.ScenarioModelBuilder
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition
import com.justai.jaicf.plugin.StateBody
import com.justai.jaicf.plugin.StateDeclaration
import com.justai.jaicf.plugin.StateName
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@DslMarker
annotation class ScenarioDsl

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
    @StateDeclaration
    fun state(
        @StateName name: String,
        noContext: Boolean = false,
        modal: Boolean = false,
        @StateBody body: StateBuilder<B, R>.() -> Unit,
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
     * @param name an optional state name ("fallback" by default)
     * @param body an action block that will be executed
     */
    @ScenarioDsl
    @StateDeclaration
    fun fallback(
        @StateName name: String = "fallback",
        @StateBody body: ActionContext<ActivatorContext, B, R>.() -> Unit,
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
     * @param channelToken a type token of the channel
     * @param name an optional state name ("fallback" by default)
     * @param body an action block that will be executed only if request matches given [channelToken]
     */
    @ScenarioDsl
    @StateDeclaration
    fun <B1 : B, R1 : R> fallback(
        channelToken: ChannelTypeToken<B1, R1>,
        @StateName name: String = "fallback",
        @StateBody body: ActionContext<ActivatorContext, B1, R1>.() -> Unit,
    ) = fallback(name) { channelToken.invoke(body) }

    /**
     * Appends top-level states of the given scenario to the current state.
     * Means that the top-level states of the [other] scenario will be
     * directly accessible from the current state.
     *
     * Hooks defined in [other] will be exposed to the current scenario if the current state is root or ignored otherwise.
     * Hooks available in the current state will be available in all appended states.
     *
     * @param other scenario to append
     */
    @ScenarioDsl
    fun append(other: Scenario) {
        val isRoot = this is RootBuilder
        doAppend(other, ignoreHooks = !isRoot, exposeHooks = isRoot, propagateHooks = true)
    }

    /**
     * Appends the root of the given scenario to the current state with name provided as [context].
     * Means that the [other] scenario will become a child of the current state
     * and all states in the [other] scenario will be resolved against the current state path.

     * The root of the [other] scenario will not be directly accessible from the current state
     * as it can't have any activators defined in its root, but it will be accessible by [Reactions.go]
     * and similar methods.
     *
     * The [other] scenario can be appended as modal scenario, i.e. the appended root of the [other] scenario
     * will become modal.
     *
     * Hooks defined in [other] will be available inside the subtree of [other]'s scenario root.
     * Hooks defined in [other] will be exposed to the current scenario if the current state is root or ignored otherwise.
     * Hooks available in the current state will be available in all appended states if [propagateHooks] is set to true.
     *
     * @param context the name of the [other] scenario root state
     * @param other scenario to append
     * @param modal whether the scenario should be modal
     * @param propagateHooks whether the scenario should inherit hooks from the current scenario
     */
    @ScenarioDsl
    @StateDeclaration
    fun append(
        @StateName context: String,
        other: Scenario,
        modal: Boolean = false,
        propagateHooks: Boolean = true
    ) {
        val isRoot = this is RootBuilder
        state(context, noContext = false, modal = modal) @StateBody {
            doAppend(other, ignoreHooks = false, exposeHooks = isRoot, propagateHooks = propagateHooks)
        }
    }

    internal fun doAppend(
        other: Scenario,
        ignoreHooks: Boolean,
        exposeHooks: Boolean,
        propagateHooks: Boolean
    ) = scenarioModelBuilder.append(path, other, ignoreHooks, exposeHooks, propagateHooks)

    internal open fun build(): State = State(path, noContext, modal)
}

class RootBuilder<B : BotRequest, R : Reactions> internal constructor(
    scenarioModelBuilder: ScenarioModelBuilder,
    channelToken: ChannelTypeToken<B, R>
) : ScenarioGraphBuilder<B, R>(scenarioModelBuilder, channelToken, StatePath.root(), false, false) {

    private var model: ScenarioModel? = null

    @ScenarioDsl
    fun <T : BotHook> handle(klass: KClass<T>, listener: @ScenarioDsl T.() -> Unit) =
        addHandler(klass) { listener.invoke(it) }

    @ScenarioDsl
    inline fun <reified T : BotHook> handle(noinline listener: @ScenarioDsl T.() -> Unit) = handle(T::class, listener)

    private fun <T : BotHook> addHandler(klass: KClass<T>, listener: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        scenarioModelBuilder.hooks += BotHookListener(klass, listener) as BotHookListener<BotHook>
    }

    /**
     * Appends top-level states of the given scenario to the current state.
     * Means that the top-level states of the [other] scenario will be
     * directly accessible from the current state.
     *
     * Hooks defined in [other] will either be ignored if [exposeHooks] is set to false
     * or exposed to the current scenario otherwise.
     * Hooks available in the current state will be available in all appended states.
     *
     * @param exposeHooks whether to expose hooks from the [other] scenario to the current scenario
     * @param other scenario to append
     */
    @ScenarioDsl
    fun append(other: Scenario, exposeHooks: Boolean = true) {
        doAppend(other, ignoreHooks = !exposeHooks, exposeHooks = exposeHooks, propagateHooks = true)
    }

    /**
     * Appends the root of the given scenario to the current state with name provided as [context].
     * Means that the [other] scenario will become a child of the current state
     * and all states in the [other] scenario will be resolved against the current state path.

     * The root of the [other] scenario will not be directly accessible from the current state
     * as it can't have any activators defined in its root, but it will be accessible by [Reactions.go]
     * and similar methods.
     *
     * The [other] scenario can be appended as modal scenario, i.e. the appended root of the [other] scenario
     * will become modal.
     *
     * Hooks defined in [other] will be available inside the subtree of [other]'s scenario root.
     * Hooks defined in [other] will be exposed to the current scenario if the [exposeHooks] is set ot true.
     * Hooks available in the current state will be available in all appended states if [propagateHooks] is set to true.
     *
     * @param context the name of the [other] scenario root state
     * @param other scenario to append
     * @param modal whether the scenario should be modal
     * @param exposeHooks whether to expose hooks to the current scenario
     * @param propagateHooks whether the scenario should inherit hooks from the current scenario
     */
    @ScenarioDsl
    @StateDeclaration
    fun append(
        @StateName context: String,
        other: Scenario,
        modal: Boolean = false,
        exposeHooks: Boolean = true,
        propagateHooks: Boolean = true
    ) {
        state(context, noContext = false, modal = modal) @StateBody {
            doAppend(other, ignoreHooks = false, exposeHooks = exposeHooks, propagateHooks = propagateHooks)
        }
    }

    internal fun buildScenario(): ScenarioModel {
        val root = build()
        return model ?: scenarioModelBuilder.apply { states += root }.build().also { model = it }
    }
}

class StateBuilder<B : BotRequest, R : Reactions> internal constructor(
    scenarioModelBuilder: ScenarioModelBuilder,
    channelToken: ChannelTypeToken<B, R>,
    private val parent: StatePath,
    private val name: String,
    noContext: Boolean,
    modal: Boolean
) : WithLogger, ScenarioGraphBuilder<B, R>(scenarioModelBuilder, channelToken, parent.resolve(name), noContext, modal) {
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
        check(action == null) { "Multiple actions are not available in a single state: $path" }
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
     * The action will be executed only if [ActionContext] type matches the given [contextToken]
     *
     * @param contextToken a full context type token
     * @param body a code block of the action
     */
    fun <A1 : ActivatorContext, B1 : B, R1 : R> action(
        contextToken: ContextTypeToken<A1, B1, R1>,
        body: @ScenarioDsl ActionContext<A1, B1, R1>.() -> Unit
    ) = action { contextToken.invoke(body) }

    internal override fun build(): State = verify().run { State(path, noContext, modal, action?.let(::ActionAdapter)) }

    private fun verify(): StateBuilder<B, R> {
        if (this.parent.isRoot && !name.matches(Regex("/?[^/]*")))
            logger.warn(
                """
                    Slashes are not allowed in the name of top-level state. Your state path: "$path"
                    Example solution: replace "state("state/child") with two separate states.
                    This may cause incorrect JAICF Intellij IDEA Plugin behaviour.
                    """.trimIndent()
            )

        if (!this.parent.isRoot && !name.matches(Regex("[^/]*")))
            logger.warn(
                """
                    Slashes are not allowed in names of inner states. Your state path: "$path"
                    Example solution: replace "state("state/child") with two separate states.
                    This may cause incorrect JAICF Intellij IDEA Plugin behaviour.
                    """.trimIndent()
            )

        if (name.matches(Regex("/?")))
            logger.warn("State name must not be empty. Your state path: $path")

        return this
    }
}
