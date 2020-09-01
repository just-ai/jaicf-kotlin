package com.justai.jaicf.reactions

import com.justai.jaicf.context.LoggingContext

/**
 * JAVADOC ME
 * */
interface Reaction

/**
 * JAVADOC ME
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
 * JAVADOC ME
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
 * JAVADOC ME
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
 * JAVADOC ME
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
 * JAVADOC ME
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
