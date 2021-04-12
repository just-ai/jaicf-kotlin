package com.justai.jaicf.logging

import com.justai.jaicf.context.ExecutionContext

/**
 * Abstraction for result of performing some reaction.
 *
 * @property fromState - state a reaction was invoked from
 *
 * @see com.justai.jaicf.reactions.Reactions
 * */
abstract class Reaction(
    open val fromState: String
)

/**
 * Result of performing reactions.say() to store in [ExecutionContext].
 *
 * @see [ExecutionContext]
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
 * Result of performing reactions.image() to store in [ExecutionContext]. May not be supported in some channels.
 *
 * @see [ExecutionContext]
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
 * Result of performing reactions.buttons() to store in [ExecutionContext]. May not be supported in some channels.
 *
 * @see [ExecutionContext]
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
 * Result of performing reactions.go() to store in [ExecutionContext].
 *
 * @see [ExecutionContext]
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
 * Result of performing reactions.changeState() to store in [ExecutionContext].
 * */
data class ChangeStateReaction internal constructor(
    val transition: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "change state from $fromState to state $transition"

    companion object
}

/**
 * Result of performing reactions.audio() to store in [ExecutionContext]. May not be supported in some channels.
 *
 * @see [ExecutionContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class AudioReaction internal constructor(
    val audioUrl: String,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "audio $audioUrl from state $fromState"

    companion object
}

/**
 * Reaction that is displayed as a carousel to store in [ExecutionContext]. May not be supported in some channels.
 *
 * @see [ExecutionContext]
 * @see [com.justai.jaicf.logging.ConversationLogger]
 * */
data class CarouselReaction internal constructor(
    val title: String,
    val elements: List<Element>,
    override val fromState: String
) : Reaction(fromState) {

    data class Element(
        val title: String,
        val buttons: List<String>,
        val description: String? = null,
        val imageUrl: String? = null,
        val buttonRedirectUrl: String? = null
    )

    override fun toString(): String = """carousel with title "$title" from state $fromState"""

    companion object
}
