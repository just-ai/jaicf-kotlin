package com.justai.jaicf.context

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.helpers.action.smartRandom
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
open class ActionContext(
    val context: BotContext,
    val activator: ActivatorContext,
    val request: BotRequest,
    val reactions: Reactions
) {

    /**
     * Returns random element.
     * Works as a smart random preserving previous calls results to guarantee that there are no two equal results in two consecutive calls.
     * @param elements a vararg of elements to select random from
     * @return a random element
     */
    fun <T> random(vararg elements: T) = elements[smartRandom(elements.size, this) % elements.size]

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
    fun Reactions.sayRandom(vararg texts: String) = say(random(*texts))
}