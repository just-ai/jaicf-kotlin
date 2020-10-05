package com.justai.jaicf.builder

import com.justai.jaicf.activator.catchall.CatchAllActivationRule
import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule
import com.justai.jaicf.activator.intent.IntentByNameActivationRule
import com.justai.jaicf.activator.regex.RegexActivationRule
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookAction
import com.justai.jaicf.hook.BotHookException
import com.justai.jaicf.model.ActionAdapter
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.model.transition.Transition
import java.util.*

/**
 * The main abstraction to build [ScenarioModel] using a scenario DSL.
 * You can use it through Scenario type alias.
 *
 * A simple example of usage:
 *
 * ```
 * object HelloWorldScenario: Scenario() {
 *  init {
 *
 *    state("main") {
 *
 *      activators {
 *        catchAll()
 *        event(AlexaEvent.LAUNCH)
 *      }
 *
 *      action {
 *        reactions.sayRandom("Hi!", "Hello there!")
 *      }
 *
 *      state("inner") {
 *        ...
 *      }
 *    }
 *
 *  }
 * }
 *
 * val helloWorldBot = BotEngine(
 *  model = HelloWorldScenario.model,
 *  activators = arrayOf(
 *    AlexaActivator,
 *    CatchAllActivator
 *  )
 * )
 * ```
 *
 * Please learn more about scenario DSL in the SDK documentation
 *
 * @param dependencies a list of scenarios that are used by this one. These scenarios will be built and appended to the current one automatically during initialization.
 * @see ScenarioModel
 */
abstract class ScenarioBuilder(
    private val dependencies: List<ScenarioBuilder> = emptyList()
) {
    internal var currentState = StateBuilder(StatePath.root())
    private val statesStack = ArrayDeque<StateBuilder>(listOf(currentState))

    val model: ScenarioModel by lazy { build() }

    private fun build(): ScenarioModel {
        return dependencies.fold(ScenarioModel()) { model, builder ->
            model + builder.model
        }
    }

    /**
     * Registers a listener for a particular [BotHook].
     * Listener will be invoked by bot engine on the corresponding phase of the user's request processing.
     * To interrupt the request processing just throw a [BotHookException] in the body of your listener.
     *
     * @param listener a listener block
     * @see BotHook
     */
    inline fun <reified T: BotHook> handle(noinline listener: (T) -> Unit) {
        model.hooks.run {
            putIfAbsent(T::class, mutableListOf())
            get(T::class)?.add(listener as BotHookAction<in BotHook>)
        }
    }

    /**
     * Appends a new top-level state to the scenario.
     *
     * @param name a name of the state. Could be plain text or contains slashes to define a state path
     * @param noContext indicates if this state should not to change the current dialogue's context
     * @param modal indicates if this state should process the user's request in modal mode ignoring all other states
     * @param body a code block of the state that contains activators, action and inner states definitions
     */
    fun state(
        name: String,
        noContext: Boolean = false,
        modal: Boolean = false,
        body: StateBuilder.() -> Unit
    ) {
        val path = currentState.path.resolve(name)
        val sb = StateBuilder(path, noContext, modal)

        currentState = sb
        statesStack.addLast(sb)

        sb.body()

        if (model.states[sb.path.toString()] != null) {
            throw IllegalStateException("Duplicated declaration of state with path: ${sb.path}")
        }

        model.states[sb.path.toString()] = sb.build()

        statesStack.removeLast()
        currentState = statesStack.last
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
     * @param action an action block that will be executed
     */
    fun fallback(
        state: String = "fallback",
        action: ActionContext.() -> Unit
    ) = state(
        name = state,
        noContext = true,
        body = {
            activators { catchAll() }
            action(action)
        }
    )

    private fun state0(
        name: String,
        noContext: Boolean,
        modal: Boolean,
        body: StateBuilder.() -> Unit) = state(name, noContext, modal, body)

    inner class StateBuilder(
        val path: StatePath,
        private val noContext: Boolean = false,
        private val modal: Boolean = false
    ) {

        private var action: (ActionContext.() -> Unit)? = null

        internal fun build() : State {
            return State(
                path,
                noContext,
                modal,
                action?.let { ActionAdapter(it) })
        }

        /**
         * Appends local activators to this state. Means that this state can be activated only if the parent state was activated previously.
         * If the state is on top of the states hierarchy then these activators become global for scenario.
         *
         * @param fromState an optional state from where this state could be activated. If not specified the parent's state is used.
         * @param body a code block that contains activators list
         * @see com.justai.jaicf.activator.Activator
         */
        fun activators(fromState: String = currentState.path.parent, body: BindBuilder.() -> Unit) {
            BindBuilder(fromState).body()
        }

        /**
         * Appends global activators for this state. Means that this state can be activated from any point of scenario.
         *
         * @param body a code block that contains activators list
         * @see com.justai.jaicf.activator.Activator
         */
        fun globalActivators(body: BindBuilder.() -> Unit) = activators("/", body)

        /**
         * An action that should be executed once this state was activated.
         * @param body a code block of the action
         */
        fun action(body: ActionContext.() -> Unit) {
            action = body
        }

        /**
         * Appends an inner state to the current state.
         * Means that inner state could be activated only if it's prarent state was activated previously.
         *
         * @param name a name of the state. Could be plain text or contains slashes to define a state path
         * @param noContext indicates if this state should not to change the current dialogue's context
         * @param modal indicates if this state should process the user's request in modal mode ignoring all other states
         * @param body a code block of the state that contains activators, action and inner states definitions
         */
        fun state(
            name: String,
            noContext: Boolean = false,
            modal: Boolean = false,
            body: StateBuilder.() -> Unit
        ) = state0(name, noContext, modal, body)

    }


    inner class BindBuilder(private val fromState: String) {
        private val toState = currentState.path.toString()

        private fun add(transition: Transition) = model.transitions.add(transition)

        /**
         * Appends catch-all activator to this state. Means that any text can activate this state.
         * Requires a [com.justai.jaicf.activator.catchall.CatchAllActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.catchall.CatchAllActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun catchAll() = add(Transition(
            fromState,
            toState,
            CatchAllActivationRule()
        ))

        /**
         * Appends regex activator to this state. Means that any text that matches to the pattern can activate this state.
         * Requires a [com.justai.jaicf.activator.regex.RegexActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.regex.RegexActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun regex(pattern: Regex) = add(Transition(
            fromState,
            toState,
            RegexActivationRule(pattern.pattern)
        ))

        /**
         * Appends regex activator to this state. Means that any text that matches to the pattern can activate this state.
         * Requires a [com.justai.jaicf.activator.regex.RegexActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.regex.RegexActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun regex(pattern: String) = regex(pattern.toRegex())

        /**
         * Appends event activator to this state. Means that an event with such name can activate this state.
         * Requires a [com.justai.jaicf.activator.event.EventActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.event.EventActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun event(event: String) = add(Transition(
                fromState,
                toState,
                EventByNameActivationRule(event)
        ))

        /**
         * Appends any-event activator to this state. Means that any intent can activate this state.
         * Requires a [com.justai.jaicf.activator.event.EventActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.event.EventActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun anyEvent() = add(Transition(
            fromState,
            toState,
            AnyEventActivationRule()
        ))

        /**
         * Appends intent activator to this state. Means that an intent with such name can activate this state.
         * Requires a [com.justai.jaicf.activator.intent.IntentActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.intent.IntentActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun intent(intent: String) = add(Transition(
            fromState,
            toState,
            IntentByNameActivationRule(intent)
        ))

        /**
         * Appends any-intent activator to this state. Means that any intent can activate this state.
         * Requires a [com.justai.jaicf.activator.intent.IntentActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
         *
         * @see com.justai.jaicf.activator.intent.IntentActivator
         * @see com.justai.jaicf.api.BotApi
         */
        fun anyIntent() = add(Transition(
            fromState,
            toState,
            AnyIntentActivationRule()
        ))
    }

}