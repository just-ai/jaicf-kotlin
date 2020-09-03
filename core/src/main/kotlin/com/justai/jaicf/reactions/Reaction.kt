package com.justai.jaicf.reactions

import com.justai.jaicf.context.LoggingContext

/**
 * Abstraction for result of performing some reaction.
 *
 * @see Reactions
 * */
interface Reaction

/**
 * Result of performing reactions.say()
 * */
data class SayReaction internal constructor(
    var text: String,
    val state: String,
    val loggingContext: LoggingContext
) : Reaction {
    init {
        loggingContext.reactions.add(this)
    }

    override fun toString(): String = """answer "$text" from state $state"""
}

/**
 * Result of performing reactions.image(). May not be supported in some channels.
 * */
data class ImageReaction internal constructor(
    val imageUrl: String,
    val state: String,
    val loggingContext: LoggingContext
) : Reaction {
    init {
        loggingContext.reactions.add(this)
    }

    override fun toString(): String = "imageUrl $imageUrl from state $state"
}

/**
 * Result of performing reactions.buttons(). May not be supported in some channels.
 * */
data class ButtonsReaction internal constructor(
    val buttons: List<String>,
    val state: String,
    val loggingContext: LoggingContext
) : Reaction {
    init {
        loggingContext.reactions.add(this)
    }

    override fun toString(): String = "buttons $buttons from state $state"
}

/**
 * Result of performing reactions.go().
 * */
data class GoReaction internal constructor(
    val transition: String,
    val state: String,
    val loggingContext: LoggingContext
) : Reaction {
    init {
        loggingContext.reactions.add(this)
    }

    override fun toString(): String = "transition from state $state to state $transition"
}

/**
 * Result of performing reactions.audio(). May not be supported in some channels.
 * */
data class AudioReaction internal constructor(
    val audioUrl: String,
    val state: String,
    val loggingContext: LoggingContext
) : Reaction {
    init {
        loggingContext.reactions.add(this)
    }

    override fun toString(): String = "audio $audioUrl from state $state"
}
