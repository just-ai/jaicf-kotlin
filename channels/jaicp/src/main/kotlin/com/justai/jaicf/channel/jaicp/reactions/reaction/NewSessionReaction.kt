package com.justai.jaicf.channel.jaicp.reactions.reaction

import com.justai.jaicf.logging.Reaction

data class NewSessionReaction(
    override val fromState: String
) : Reaction(fromState)