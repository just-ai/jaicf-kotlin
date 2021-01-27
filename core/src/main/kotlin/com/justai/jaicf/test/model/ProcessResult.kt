package com.justai.jaicf.test.model

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.logging.*
import com.justai.jaicf.reactions.Reactions

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
    val executionContext = reactions.executionContext

    val reactionList = reactions.executionContext.reactions

    val answer = getAllForType<SayReaction>().joinToString(separator = "\n") { it.text }.trimIndent()

    inline fun <reified T : Reaction> getAllForType() = reactionList.filterIsInstance<T>()

    /**
     * JAVADOC ME
     * */
    inline fun <reified T : Reaction> hasReaction(reaction: T) {
        assertTrue(reactionList.contains(reaction))
    }

    /**
     * Asserts that scenario was in state [state] before processing request
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun startsWithState(state: String) = apply {
        assertEquals(state, executionContext.firstState)
    }

    /**
     * Asserts a state of scenario that was initially activated by this request
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun goesToState(state: String) = apply {
        assertEquals(state, executionContext.activationContext?.activation?.state)
    }

    /**
     * Asserts a state of scenario was visited during request processing
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun visitsState(state: String) = apply {
        assertTrue(getAllForType<GoReaction>().any { it.transition == state }) // TODO: this is invalid
    }

    infix fun endsWithState(context: String) = apply {
        assertEquals(context, botContext.dialogContext.currentContext)
    }

    infix fun hasAnswer(text: String) = apply {
        assertTrue(getAllForType<SayReaction>().any { it.text == text })
    }

    infix fun hasButtons(buttons: List<String>) = apply {
        assertTrue(getAllForType<ButtonsReaction>().any { it.buttons == buttons })
    }

    infix fun hasImage(image: String) = apply {
        assertTrue(getAllForType<ImageReaction>().any { it.imageUrl == image })
    }

    infix fun hasAudio(audio: String) = apply {
        assertTrue(getAllForType<AudioReaction>().any { it.audioUrl == audio })
    }

    /**
     * Asserts an expected text response with an actual one if [com.justai.jaicf.reactions.TestReactions] was used in this test
     *
     * @param text an expected test of response
     * @return this [ProcessResult] for chaining
     */
    infix fun responds(text: String) = apply {
        assertEquals(text, answer)
    }

    /**
     * Asserts a result that was returned from scenario
     *
     * @param result a value of an expected result
     * @return this [ProcessResult] for chaining
     */
    infix fun returnsResult(result: Any?) = apply { assertEquals(result, botContext.result) }
}