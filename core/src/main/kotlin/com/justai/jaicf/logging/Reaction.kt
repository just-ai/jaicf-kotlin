package com.justai.jaicf.logging

/**
 * Abstraction for result of performing some reaction.
 *
 * @property fromState - a state which had invoked a reaction
 *
 * @see com.justai.jaicf.reactions.Reactions
 * */
abstract class Reaction(
    open val fromState: String
)

/**
 * Result of performing reactions.say() to store in [LoggingContext].
 *
 * @see [com.justai.jaicf.logging.LoggingContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class SayReaction internal constructor(
    val text: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = """reply "$text" from state $fromState"""

    companion object
}

/**
 * Result of performing reactions.image() to store in [LoggingContext]. May not be supported in some channels.
 *
 * @see [com.justai.jaicf.logging.LoggingContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class ImageReaction internal constructor(
    val imageUrl: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "imageUrl $imageUrl from state $fromState"

    companion object
}

/**
 * Result of performing reactions.buttons() to store in [LoggingContext]. May not be supported in some channels.
 *
 * @see [com.justai.jaicf.logging.LoggingContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class ButtonsReaction internal constructor(
    val buttons: List<String>,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "buttons $buttons from state $fromState"

    companion object
}

/**
 * Result of performing reactions.go() to store in [LoggingContext].
 *
 * @see [com.justai.jaicf.logging.LoggingContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class GoReaction internal constructor(
    val transition: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "transition from state $fromState to state $transition"

    companion object
}

/**
 * Result of performing reactions.changeState() to store in [LoggingContext]..
 * */
data class ChangeStateReaction internal constructor(
    val transition: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "change state from $fromState to state $transition"

    companion object
}

/**
 * Result of performing reactions.audio() to store in [LoggingContext]. May not be supported in some channels.
 *
 * @see [com.justai.jaicf.logging.LoggingContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class AudioReaction internal constructor(
    val audioUrl: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "audio $audioUrl from state $fromState"

    companion object
}
