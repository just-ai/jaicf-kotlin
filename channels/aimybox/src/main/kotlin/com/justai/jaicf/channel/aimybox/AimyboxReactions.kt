package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.*
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions

val Reactions.aimybox
    get() = this as? AimyboxReactions

class AimyboxReactions(
    override val response: AimyboxBotResponse
): ResponseReactions<AimyboxBotResponse>(response) {

    fun addReply(reply: AimyboxReply) {
        response.replies.add(reply)
    }

    override fun say(text: String) = say(text, null, null)

    fun say(text: String, tts: String? = null, lang: String? = null) {
        addReply(TextReply(text, tts, lang))
        if (response.text.isNullOrEmpty()) {
            response.text = text
        }
    }

    fun ask(text: String, tts: String? = null, lang: String? = null) {
        question(true)
        say(text, tts, lang)
    }

    fun question(question: Boolean) {
        response.question = question
    }

    fun audio(url: String) = addReply(AudioReply(url))

    override fun image(url: String) = addReply(ImageReply(url))

    override fun buttons(vararg buttons: String) = buttons(
        *buttons.map { TextButton(it) }.toTypedArray()
    )

    fun buttons(vararg buttons: Button) = addReply(
        ButtonsReply(buttons.toList())
    )
}