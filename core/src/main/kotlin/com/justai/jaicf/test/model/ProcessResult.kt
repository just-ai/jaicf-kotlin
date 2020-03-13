package com.justai.jaicf.test.model

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.text

import org.junit.jupiter.api.Assertions.*

/**
 * Represents a result of test running.
 * Provides a set of helper functions to assert test results in infix manner. These functions could be used in form of channel. For example:
 *
 * ```
 * @Test
 * fun `Greets new user`() {
 *   query("Hello there") responds "Hello" goesToState "/start" endsWithState "/how_are_you"
 * }
 * ```
 *
 * @property botContext an actual [BotContext] after test running
 * @property reactions reactions instance that was used during the test
 */
data class ProcessResult(
    val botContext: BotContext,
    val reactions: Reactions
) {
    /**
     * Asserts an expected text response with an actual one if [com.justai.jaicf.reactions.TextReactions] was used in this test
     *
     * @param text an expected test of response
     * @return this [ProcessResult] for chaining
     */
    infix fun responds(text: String) = assertEquals(text, reactions.text?.response?.text).run { this@ProcessResult }

    /**
     * Asserts a state of scenario that was initially activated by this request
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun goesToState(state: String) = assertEquals(state, botContext.dialogContext.currentState).run { this@ProcessResult }

    /**
     * Asserts a current state of scenario that was activated the last in the chain of states
     *
     * @param context a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun endsWithState(context: String) = assertEquals(context, botContext.dialogContext.currentContext).run { this@ProcessResult }

    /**
     * Asserts a result that was returned from scenario
     *
     * @param result a value of an expected result
     * @return this [ProcessResult] for chaining
     */
    infix fun returnsResult(result: Any?) = assertEquals(result, botContext.result).run { this@ProcessResult }
}