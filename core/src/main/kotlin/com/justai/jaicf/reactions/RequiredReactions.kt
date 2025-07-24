package com.justai.jaicf.reactions

import com.justai.jaicf.logging.SayReaction

/**
 * A base reactions interface.
 * Defines a list of required reactions that must be implemented by each particular channel-related [Reactions]
 */
interface RequiredReactions {
    /**
     * Appends a raw text reply to the response.
     * This method should be implemented by every particular channel-related [Reactions].
     *
     * @param text a raw text to append to the response
     */
    fun say(text: String): SayReaction
}