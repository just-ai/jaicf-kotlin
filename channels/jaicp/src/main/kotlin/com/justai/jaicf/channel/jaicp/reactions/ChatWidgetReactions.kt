package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.Button
import com.justai.jaicf.channel.jaicp.dto.ButtonsReply
import com.justai.jaicf.channel.jaicp.dto.CarouselReply
import com.justai.jaicf.channel.jaicp.dto.CarouselReply.CarouselSlide
import com.justai.jaicf.channel.jaicp.dto.ImageReply
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.CarouselReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.buttons
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

val Reactions.chatwidget
    get() = this as? ChatWidgetReactions

class ChatWidgetReactions : JaicpReactions(), JaicpCompatibleAsyncReactions {
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

    fun carousel(text: String, vararg slides: CarouselSlide): CarouselReaction {
        replies.add(CarouselReply(text, slides.asList()))

        return CarouselReaction.create(text, slides.toReactionSlides())
    }

    // TODO: Move from class body
    private fun Array<out CarouselSlide>.toReactionSlides() = map {
        CarouselReaction.Element(
            title = it.title,
            buttons = listOf(it.buttonText),
            description = it.description,
            imageUrl = it.imageUrl,
            buttonRedirectUrl = it.buttonRedirectUrl
        )
    }
}
