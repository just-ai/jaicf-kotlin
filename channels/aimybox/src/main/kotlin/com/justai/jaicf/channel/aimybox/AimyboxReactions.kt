package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.*
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.*

val Reactions.aimybox
    get() = this as? AimyboxReactions

class AimyboxReactions(
    override val response: AimyboxBotResponse
) : ResponseReactions<AimyboxBotResponse>(response) {

    fun addReply(reply: AimyboxReply) {
        response.replies.add(reply)
    }

    override fun say(text: String) = say(text, null, null)

    fun say(text: String, tts: String? = null, lang: String? = null): SayReaction {
        addReply(TextReply(text, tts, lang))
        if (response.text.isNullOrEmpty()) {
            response.text = text
        }

        return SayReaction.create(text)
    }

    fun question(question: Boolean) {
        response.question = question
    }

    override fun audio(url: String): AudioReaction {
        addReply(AudioReply(url))
        return AudioReaction.create(url)
    }

    override fun image(url: String): ImageReaction {
        addReply(ImageReply(url))
        return ImageReaction.create(url)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        return buttons(*buttons.map { TextButton(it) }.toTypedArray())
    }

    fun buttons(vararg buttons: Button): ButtonsReaction {
        addReply(ButtonsReply(buttons.toList()))
        return ButtonsReaction.create(buttons.toList().map { it.text })
    }

    fun endConversation() = question(false)
}