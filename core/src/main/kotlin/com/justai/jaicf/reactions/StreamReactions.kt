package com.justai.jaicf.reactions

import java.util.stream.Stream

/**
 * Reactions that support response streaming.
 */
interface StreamReactions : RequiredReactions {
    /**
     * Appends a stream of texts to the response.
     * By default, collects a stream elements to a resulting text and appends it to the response.
     * This behavior can be overwritten in custom [Reactions] if needed.
     *
     * @param stream a stream of texts to append to the response
     */
    fun say(stream: Stream<String>) = say(stream.toList().joinToString(""))
}

val Reactions.stream
    get() = this as? StreamReactions