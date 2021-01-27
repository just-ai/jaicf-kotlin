package com.justai.jaicf.builder

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookAction
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@DslMarker
annotation class ScenarioDsl

@ScenarioDsl
class ScenarioBuilder<B : BotRequest, R : Reactions> internal constructor(
    private val channelToken: ChannelTypeToken<B, R>
) {
    private val scenarioModelBuilder = ScenarioModelBuilder()
    private var model: ScenarioModel? = null

    /**
     * Registers a listener for a particular [BotHook].
     * Listener will be invoked by bot engine on the corresponding phase of the user's request processing.
     * To interrupt the request processing just throw a [BotHookException] in the body of your listener.
     *
     * @param klass a [BotHook] type
     * @param listener a listener block
     * @see BotHook
     */
    @ScenarioDsl
    fun <T : BotHook> handle(klass: KClass<T>, listener: Dummy.(T) -> Unit) = addHandler(klass) { Dummy.listener(it) }

    @ScenarioDsl
    inline fun <reified T : BotHook> handle(noinline listener: Dummy.(T) -> Unit) = handle(T::class, listener)

    private fun <T: BotHook> addHandler(klass: KClass<T>, listener: (T) -> Unit) {
        val hooks = scenarioModelBuilder.hooks.computeIfAbsent(klass) { mutableListOf() }
        @Suppress("UNCHECKED_CAST")
        hooks += listener as BotHookAction<in BotHook>
    }

    /**
     * Appends given scenarios to the current.
     * Means that the scenarios will be accessible from the current model.
     *
     * The scenarios will be merged as follows:
     * - all top-level states of each scenario will be linked to a single root
     *   resulting in a single merged scenario
     *
     * @param first the first of additional scenarios
     * @param others other additional scenarios
     */
    @ScenarioDsl
    fun append(first: Scenario, vararg others: Scenario) {
        scenarioModelBuilder.dependencies += first.model
        scenarioModelBuilder.dependencies += others.map { it.model }
    }

    /**
     * The starting point of a scenario building, builds the root state of the scenario.
     * Every conversation starts in the root state.
     *
     * @param body a code block that builds the root state.
     *
     * @see RootBuilder
     */
    @ScenarioDsl
    fun start(body: RootBuilder<B, R>.() -> Unit) {
        val root = RootBuilder(scenarioModelBuilder, channelToken).apply(body).build()
        scenarioModelBuilder.states.add(root)
    }

    internal fun build(): ScenarioModel = model ?: scenarioModelBuilder.build().also { model = it }
}

@ScenarioDsl
object Dummy