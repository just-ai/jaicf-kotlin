package com.justai.jaicf.context

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.generic.ActivatorTypeToken
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken
import com.justai.jaicf.helpers.action.smartRandom
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.reactions.Reactions
import kotlin.random.Random

/**
 * The context of the request execution. Every action code block of the scenario is executed in this context.
 * Contains properties related to the user's data, activator's details, request's details and reactions API.
 * Also provides some helpful methods for response building.
 *
 * @property context a [BotContext] instance that contains user-related and dialogue state data
 * @property activator a particular [ActivatorContext] of the activator that handled the user's request
 * @property request a particular channel-related [BotRequest] that contains request's details
 * @property reactions a particular channel-related [Reactions] that contains methods for building and sending a response
 *
 * @see [BotContext]
 * @see [ActivatorContext]
 * @see [BotRequest]
 * @see [Reactions]
 */
open class ActionContext<A: ActivatorContext, B: BotRequest, R: Reactions>(
    open val context: BotContext,
    open val activator: A,
    open val request: B,
    open val reactions: R
) {

    /**
     * Returns random element.
     * Works as a smart random preserving previous calls results to guarantee that there are no two equal results in two consecutive calls.
     * @param elements a list of elements to select random from
     * @return a random element
     */
    fun <T> random(elements: List<T>) = elements[smartRandom(elements.size, this) % elements.size]

    /**
     * Returns random element.
     * @param elements a vararg of elements to select random from
     * @return a random element
     */
    fun <T> random(vararg elements: T) = random(elements.asList())

    /**
     * Returns random [Int] between 0 (inclusive) and [max] (exclusive).
     * Works as a smart random preserving previous calls results to guarantee that there are no two equal results in two consecutive calls.
     * @param max an upper bound
     * @return a random [Int]
     */
    open fun random(max: Int) = smartRandom(max, this)

    /**
     * Returns random [Int] between [min] (inclusive) and [max] (exclusive).
     * Works as a usual Random.nextInt
     *
     * @param min a lower bound
     * @param max an upper bound
     * @return a random [Int]
     */
    open fun random(min: Int, max: Int) = Random.nextInt(min, max)

    /**
     * A helper function that puts a randomized phrase into the response.
     * Utilizes smart random function to guarantee that there are no two equal phrases was returned to the user in two consecutive requests.
     * @param texts a vararg of texts to select random from
     */
    fun Reactions.sayRandom(vararg texts: String) = sayRandom(texts.asList())

    /**
     * A helper function that puts a randomized phrase into the response.
     * Utilizes smart random function to guarantee that there are no two equal phrases was returned to the user in two consecutive requests.
     * @param texts a list of String to select random from
     */
    fun Reactions.sayRandom(texts: List<String>) = say(random(texts))

    infix fun ButtonsReaction?.toStates(states: List<String>) {
        this?.buttons?.zip(states)?.forEach { (text, state) ->
            context.dialogContext.transitions[text.toLowerCase()] =
                StatePath.parse(context.dialogContext.currentState).resolve(state).toString()
        }
    }

    operator fun <A1: A, T> ActivatorTypeToken<A1>.invoke(action: ActionContext<A1, B, R>.() -> T): T? {
        return if (isInstance(activator)) {
            @Suppress("UNCHECKED_CAST")
            (this@ActionContext as ActionContext<A1, B, R>).action()
        } else {
            null
        }
    }

    operator fun <B1: B, R1: R, T> ChannelTypeToken<B1, R1>.invoke(action: ActionContext<A, B1, R1>.() -> T): T? {
        return if (isInstance(request) && isInstance(reactions)) {
            @Suppress("UNCHECKED_CAST")
            (this@ActionContext as ActionContext<A, B1, R1>).action()
        } else {
            null
        }
    }

    operator fun <A1: A, B1: B, R1: R, T> ContextTypeToken<A1, B1, R1>.invoke(action: ActionContext<A1, B1, R1>.() -> T): T? {
        return if (isInstance(activator)) {
            @Suppress("UNCHECKED_CAST")
            (this@ActionContext as ActionContext<A1, B1, R1>).action()
        } else {
            null
        }
    }
}