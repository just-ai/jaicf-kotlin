package com.justai.jaicf.reactions

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.logging.*
import com.justai.jaicf.model.state.StatePath

/**
 * A base abstraction for channel-related reactions.
 * This class contains a collection of base methods for scenario state managing and methods to create [Reaction] objects.
 * Also there is a place for response building methods that should be implemented by every channel-related reactions class.
 *
 * Usage example:
 *
 * ```
 * state("start") {
 *   activators {
 *     catchAll()
 *   }
 *
 *   action {
 *     reactions.say("Hello! How are you?")
 *     reactions.changeState("/ask/mood")
 *   }
 * }
 * ```
 * @see BotContext
 * @see ResponseReactions
 * @see Reaction
 */
abstract class Reactions : ReactionRegistrar() {

    override lateinit var botContext: BotContext

    override lateinit var loggingContext: LoggingContext

    /**
     * Changes the state of scenario and executes it's action block immediately.
     *
     * @param path the path of the state to jump to. May be absolute or relative.
     * @param callbackState an optional callback state path. Bot engine activates this state once the sub-scenario returned some result through goBack or changeStateBack methods.
     */
    fun go(path: String, callbackState: String? = null): GoReaction {
        val dialogContext = botContext.dialogContext
        val currentState = StatePath.parse(dialogContext.currentState)
        val resolved = currentState.resolve(path).toString()
        dialogContext.nextState = resolved

        callbackState?.let {
            dialogContext.backStateStack.push(currentState.resolve(it).toString())
        }
        return GoReaction.create(resolved)
    }

    /**
     * Changes the state of scenario but doesn't execute it's action block.
     *
     * @param path the path of the state to jump to. May be absolute or relative.
     * @param callbackState an optional callback state path. Bot engine activates this state once the sub-scenario returned some result through goBack or changeStateBack methods.
     */
    fun changeState(path: String, callbackState: String? = null): ChangeStateReaction {
        val dialogContext = botContext.dialogContext
        val currentState = StatePath.parse(dialogContext.currentState)
        val resolved = currentState.resolve(path).toString()
        dialogContext.nextContext = resolved

        callbackState?.let {
            dialogContext.backStateStack.push(currentState.resolve(it).toString())
        }
        return ChangeStateReaction.create(resolved)
    }

    /**
     * Changes the state of scenario to the callback state if it was provided through go of changeState methods.
     * Immediately invokes an action block of callback state.
     *
     * @param result an optional result that can be returned to the callback state
     */
    fun goBack(result: Any? = null): String? {
        val dialogContext = botContext.dialogContext
        val state = dialogContext.backStateStack.poll()
        result?.let { botContext.result = it }
        return state?.also {
            go(it)
        }
    }

    /**
     * Changes the state of scenario to the callback state if it was provided through go of changeState methods.
     * Doesn't invoke an action block of callback state.
     *
     * @param result an optional result that can be returned to the callback state
     */
    fun changeStateBack(result: Any? = null): String? {
        val dialogContext = botContext.dialogContext
        val state = dialogContext.backStateStack.poll()
        result?.let { botContext.result = it }
        return state?.also {
            changeState(it)
        }
    }

    /**
     * Appends a raw text reply to the response.
     * This method should be implemented by every particular channel-related [Reactions].
     *
     * @param text a raw text to append to the response
     */
    abstract fun say(text: String): SayReaction

    /**
     * Appends image to the response.
     * Not every channel supports this type of reply.
     *
     * @param url a full URL of the image file
     */
    open fun image(url: String): ImageReaction = ImageReaction(url, currentState)

    /**
     * Appends buttons to the response.
     * Not every channel supports this type of reply.
     *
     * @param buttons a collection of text buttons
     */
    open fun buttons(vararg buttons: String): ButtonsReaction = ButtonsReaction(buttons.asList(), currentState)

    /**
     * Appends audio to the response
     * Not every channels supports this type of reply.
     *
     * @param url of audio
     * */
    open fun audio(url: String): AudioReaction = AudioReaction(url, currentState)
}

typealias ButtonToState = Pair<String, String>

/**
 * Appends buttons with transitions to response.
 * When button is clicked, a corresponding state will activate
 *
 * @param buttons a collection with button texts to states
 * */
fun Reactions.buttons(vararg buttons: ButtonToState): ButtonsReaction {
    buttons.forEach { (text, transition) ->
        botContext.dialogContext.transitions[text] =
            StatePath.parse(botContext.dialogContext.currentState).resolve(transition).toString()
    }
    return buttons(*buttons.map { it.first }.toTypedArray())
}
