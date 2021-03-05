package com.justai.jaicf.channel.jaicp.reactions.reaction

import com.justai.jaicf.logging.Reaction

data class EndSessionReaction(
    override val fromState: String
) : Reaction(fromState) {
    override fun toString(): String = """end current session from state $fromState"""
}