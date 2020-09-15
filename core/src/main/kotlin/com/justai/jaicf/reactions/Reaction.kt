package com.justai.jaicf.reactions

/**
 * Abstraction for result of performing some reaction.
 *
 * @property fromState - state which invoked reaction
 *
 * @see Reactions
 * */
abstract class Reaction(
    open val fromState: String
)

/**
 * Result of performing reactions.say()
 * */
data class SayReaction internal constructor(
    val text: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = """answer "$text" from state $fromState"""

    companion object
}

/**
 * Result of performing reactions.image(). May not be supported in some channels.
 * */
data class ImageReaction internal constructor(
    val imageUrl: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "imageUrl $imageUrl from state $fromState"

    companion object
}

/**
 * Result of performing reactions.buttons(). May not be supported in some channels.
 * */
data class ButtonsReaction internal constructor(
    val buttons: List<String>,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "buttons $buttons from state $fromState"

    companion object
}

/**
 * Result of performing reactions.go().
 * */
data class GoReaction internal constructor(
    val transition: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "transition from state $fromState to state $transition"

    companion object
}

/**
 * Result of performing reactions.audio(). May not be supported in some channels.
 * */
data class AudioReaction internal constructor(
    val audioUrl: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "audio $audioUrl from state $fromState"

    companion object
}
