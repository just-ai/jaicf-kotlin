package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.Button
import com.justai.jaicf.channel.jaicp.dto.ButtonsReply
import com.justai.jaicf.channel.jaicp.dto.ImageReply
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.currentState
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.buttons
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import com.justai.jaicf.reactions.toState
import kotlinx.serialization.json.JsonElement

val Reactions.chatapi
    get() = this as? ChatApiReactions

@Suppress("MemberVisibilityCanBePrivate")
class ChatApiReactions(
    override val liveChatProvider: JaicpLiveChatProvider,
) : JaicpReactions(), JaicpCompatibleAsyncReactions {

    override fun image(url: String): ImageReaction {
        return image(imageUrl = url, caption = null)
    }

    fun image(imageUrl: String, caption: String? = null): ImageReaction {
        replies.add(ImageReply(imageUrl, caption, currentState))
        return ImageReaction.create(imageUrl)
    }

    fun button(text: String, transition: String? = null): ButtonsReaction =
        transition?.let { buttons(text toState transition) } ?: buttons(text)

    override fun buttons(vararg buttons: String): ButtonsReaction {
        return buttons(buttons.asList())
    }

    fun buttons(buttons: List<String>): ButtonsReaction {
        replies.add(ButtonsReply(buttons.map { Button(it) }, currentState))
        return ButtonsReaction.create(buttons)
    }

    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url.toUrl(), currentState))
        return AudioReaction.create(url)
    }

    fun addResponseData(data: Map<String, JsonElement>) = responseData.putAll(data)

    fun addResponseData(key: String, value: JsonElement) = addResponseData(key to value)

    fun addResponseData(element: Pair<String, JsonElement>) = addResponseData(mapOf(element))
}