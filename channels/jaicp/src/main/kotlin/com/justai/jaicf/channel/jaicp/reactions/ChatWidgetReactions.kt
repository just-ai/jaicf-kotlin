package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.Button
import com.justai.jaicf.channel.jaicp.dto.ButtonsReply
import com.justai.jaicf.channel.jaicp.dto.CarouselReply
import com.justai.jaicf.channel.jaicp.dto.ImageReply
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.CarouselReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.currentState
import com.justai.jaicf.plugin.PathValue
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.buttons
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import com.justai.jaicf.reactions.toState

val Reactions.chatwidget
    get() = this as? ChatWidgetReactions

class ChatWidgetReactions(
    override val liveChatProvider: JaicpLiveChatProvider
) : JaicpReactions(), JaicpCompatibleAsyncReactions {

    override fun image(url: String): ImageReaction {
        return image(imageUrl = url, caption = null)
    }

    fun image(imageUrl: String, caption: String? = null): ImageReaction {
        replies.add(ImageReply(imageUrl, caption, currentState))
        return ImageReaction.create(imageUrl)
    }

    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url, currentState))
        return AudioReaction.create(url)
    }

    fun button(text: String, @PathValue transition: String? = null): ButtonsReaction =
        transition?.let { buttons(text toState transition) } ?: buttons(text)

    override fun buttons(vararg buttons: String): ButtonsReaction {
        return buttons(buttons.asList())
    }

    fun buttons(buttons: List<String>): ButtonsReaction {
        replies.add(ButtonsReply(buttons.map { Button(it) }, currentState))
        return ButtonsReaction.create(buttons)
    }

    fun carousel(text: String, vararg elements: CarouselReply.Element): CarouselReaction {
        replies.add(CarouselReply(text, elements.asList(), currentState))
        return CarouselReaction.create(text, elements.toCarouselReactionElements())
    }
}

private fun Array<out CarouselReply.Element>.toCarouselReactionElements() = map {
    CarouselReaction.Element(
        title = it.title,
        buttons = listOf(CarouselReaction.Button(it.button, it.buttonRedirectUrl)),
        description = it.description,
        imageUrl = it.imageUrl
    )
}
