package com.justai.jaicf.channel.viber.reaction

import com.justai.jaicf.logging.Reaction

data class StickerReaction internal constructor(
    val stickerId: Long,
    override val fromState: String
) : Reaction(fromState) {

    override fun toString(): String = "sticker with id $stickerId from state $fromState"

    companion object
}
