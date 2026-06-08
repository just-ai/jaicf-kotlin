package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.max.api.MaxBotApi
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

val Reactions.max get() = this as? MaxReactions

/**
 * Reactions for the Max channel. Stub — send logic implemented in VS-13662.
 */
class MaxReactions(
    val api: MaxBotApi,
    val request: MaxBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?
) : Reactions(), JaicpCompatibleAsyncReactions {

    override fun say(text: String): SayReaction = TODO("VS-13662: MaxReactions.say")
    override fun image(url: String): ImageReaction = TODO("VS-13662: MaxReactions.image")
    override fun audio(url: String): AudioReaction = TODO("VS-13662: MaxReactions.audio")
    override fun buttons(vararg buttons: String): ButtonsReaction = TODO("VS-13662: MaxReactions.buttons")
}
