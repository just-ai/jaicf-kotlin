package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.jaicp.dto.Button
import com.justai.jaicf.channel.jaicp.dto.ButtonsReply
import com.justai.jaicf.channel.jaicp.dto.ImageReply
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.buttons
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import kotlinx.serialization.json.JsonObject

val Reactions.chatapi
    get() = this as? ChatApiReactions

class ChatApiReactions(
    override val liveChatProvider: JaicpLiveChatProvider,
) : JaicpReactions(), JaicpCompatibleAsyncReactions {

    override fun image(url: String): ImageReaction {
        return image(imageUrl = url, caption = null)
    }

    fun image(imageUrl: String, caption: String? = null): ImageReaction {
        replies.add(ImageReply(imageUrl, caption))
        return ImageReaction.create(imageUrl)
    }

    fun button(text: String, transition: String? = null): ButtonsReaction =
        transition?.let { buttons(text to transition) } ?: buttons(text)

    override fun buttons(vararg buttons: String): ButtonsReaction {
        return buttons(buttons.asList())
    }

    fun buttons(buttons: List<String>): ButtonsReaction {
        replies.add(ButtonsReply(buttons.map { Button(it) }))
        return ButtonsReaction.create(buttons)
    }

    fun addResponseData(data: Map<String, JsonObject>) {
        responseData.putAll(data)
    }

    fun addResponseData(key: String, value: JsonObject) {
        addResponseData(mapOf(key to value))
    }
}