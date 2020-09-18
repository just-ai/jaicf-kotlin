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

    fun say(text: String, tts: String? = null, lang: String? = null) {
        addReply(TextReply(text, tts, lang))
        if (response.text.isNullOrEmpty()) {
            response.text = text
        }

        SayReaction.register(text)
    }

    fun question(question: Boolean) {
        response.question = question
    }

    override fun audio(url: String) {
        addReply(AudioReply(url))
        AudioReaction.register(url)
    }

    override fun image(url: String) {
        addReply(ImageReply(url))
        ImageReaction.register(url)
    }

    override fun buttons(vararg buttons: String) {
        buttons(*buttons.map { TextButton(it) }.toTypedArray())
        ButtonsReaction.register(buttons.asList())
    }

    fun buttons(vararg buttons: Button) {
        addReply(ButtonsReply(buttons.toList()))
    }

    fun endConversation() = question(false)
}