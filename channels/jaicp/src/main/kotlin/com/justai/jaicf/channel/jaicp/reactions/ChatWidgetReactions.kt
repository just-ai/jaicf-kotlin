package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.Button
import com.justai.jaicf.channel.jaicp.dto.ButtonsReply
import com.justai.jaicf.channel.jaicp.dto.ImageReply
import com.justai.jaicf.reactions.Reactions

val Reactions.chatwidget
    get() = this as? ChatWidgetReactions

class ChatWidgetReactions : JaicpReactions() {
    override fun image(url: String) {
        replies.add(ImageReply(url))
    }

    fun image(imageUrl: String, caption: String? = null) {
        replies.add(ImageReply(imageUrl, caption))
    }

    fun button(text: String, transition: String? = null) {
        replies.add(
            ButtonsReply(
                Button(
                    text,
                    transition
                )
            )
        )
    }

    override fun buttons(vararg buttons: String) {
        replies.add(ButtonsReply(buttons.map { b ->
            Button(
                b
            )
        }))
    }

    fun buttons(buttons: List<String>) {
        replies.add(ButtonsReply(buttons.map { b ->
            Button(
                b
            )
        }))
    }
}