package com.justai.jaicf.test.model

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.GoReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.Reaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

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
    private val executionContext = reactions.executionContext

    val reactionList = reactions.executionContext.reactions

    val answer = getAllForType<SayReaction>().joinToString(separator = "\n") { it.text }.trimIndent()

    /**
     * Asserts a reaction registered during scenario execution
     *
     * @param reaction a [Reaction] from scenario
     * */
    inline fun <reified T : Reaction> hasReaction(reaction: T) = apply {
        assertTrue(reactionList.contains(reaction))
    }

    /**
     * Asserts that scenario was in state [context] before processing request
     *
     * @param context a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun startsWithContext(context: String) = apply {
        assertEquals(context, executionContext.firstState, "scenario execution did not start from context $context")
    }

    /**
     * Asserts a state of scenario that was initially activated by this request
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun goesToState(state: String) = apply {
        assertEquals(
            state,
            executionContext.activationContext?.activation?.state,
            "scenario execution did not start in state: $state"
        )
    }

    /**
     * Asserts a state of scenario was visited during request processing
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun visitsState(state: String) = apply {
        val anyGoReaction = getAllForType<GoReaction>().any { it.transition == state }
        val isActivationState = executionContext.activationContext?.activation?.state == state
        assertTrue(isActivationState || anyGoReaction, "transition to state $state not found")
    }

    /**
     * Asserts a state request ended processing in.
     *
     * @param state a full path of the expected state
     * @return this [ProcessResult] for chaining
     */
    infix fun endsWithState(state: String) = apply {
        assertEquals(state, botContext.dialogContext.currentContext, "scenario execution did not end in state $state")
    }

    /**
     * Asserts a state request ended processing in.
     *
     * @param text an expected test of single reply
     * @return this [ProcessResult] for chaining
     */
    infix fun hasAnswer(text: String) = apply {
        assertTrue(getAllForType<SayReaction>().any { it.text == text }, "answer \"$text\" not found.")
    }

    /**
     * Asserts buttons sent from scenario
     *
     * @param buttons an expected list of buttons sent from scenario
     * @return this [ProcessResult] for chaining
     */
    infix fun hasButtons(buttons: List<String>) = apply {
        assertTrue(
            getAllForType<ButtonsReaction>().any { it.buttons == buttons },
            "buttons ${buttons.joinToString()} not found"
        )
    }

    /**
     * Asserts an image sent from scenario
     *
     * @param image an expected image url sent from scenario
     * @return this [ProcessResult] for chaining
     */
    infix fun hasImage(image: String) = apply {
        assertTrue(getAllForType<ImageReaction>().any { it.imageUrl == image }, "image $image not found")
    }

    /**
     * Asserts an audio sent from scenario
     *
     * @param audio an expected audio url sent from scenario
     * @return this [ProcessResult] for chaining
     */
    infix fun hasAudio(audio: String) = apply {
        assertTrue(getAllForType<AudioReaction>().any { it.audioUrl == audio }, "audio $audio not found")
    }

    /**
     * Asserts an expected text response with an actual one if [com.justai.jaicf.test.reactions.TestReactions] was used in this test
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

    private inline fun <reified T : Reaction> getAllForType() = reactionList.filterIsInstance<T>()
}
