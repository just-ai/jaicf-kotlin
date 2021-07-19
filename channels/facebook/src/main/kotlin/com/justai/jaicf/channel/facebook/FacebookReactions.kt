package com.justai.jaicf.channel.facebook

import com.github.messenger4j.send.MessagePayload
import com.github.messenger4j.send.MessageResponse
import com.github.messenger4j.send.MessagingType
import com.github.messenger4j.send.Payload
import com.github.messenger4j.send.message.Message
import com.github.messenger4j.send.message.RichMediaMessage
import com.github.messenger4j.send.message.TemplateMessage
import com.github.messenger4j.send.message.TextMessage
import com.github.messenger4j.send.message.quickreply.TextQuickReply
import com.github.messenger4j.send.message.richmedia.RichMediaAsset
import com.github.messenger4j.send.message.richmedia.UrlRichMediaAsset
import com.github.messenger4j.send.message.template.ButtonTemplate
import com.github.messenger4j.send.message.template.GenericTemplate
import com.github.messenger4j.send.message.template.button.Button
import com.github.messenger4j.send.message.template.button.CallButton
import com.github.messenger4j.send.message.template.button.LogInButton
import com.github.messenger4j.send.message.template.button.LogOutButton
import com.github.messenger4j.send.message.template.button.PostbackButton
import com.github.messenger4j.send.message.template.button.UrlButton
import com.justai.jaicf.channel.facebook.api.CarouselElement
import com.justai.jaicf.channel.facebook.api.FacebookBotRequest
import com.justai.jaicf.channel.facebook.api.toTemplateElement
import com.justai.jaicf.channel.facebook.messenger.Messenger
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.CarouselReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import java.net.URL
import java.util.*

val Reactions.facebook
    get() = this as? FacebookReactions

class FacebookReactions(
    private val messenger: Messenger,
    val request: FacebookBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?
) : Reactions(), JaicpCompatibleAsyncReactions {

    fun send(payload: Payload): MessageResponse? {
        return messenger.send(payload)
    }

    fun sendResponse(message: Message) = send(
        MessagePayload.create(request.event.senderId(), MessagingType.RESPONSE, message)
    )

    fun sendUrlRichMediaResponse(url: String, type: RichMediaAsset.Type) =
        sendResponse(RichMediaMessage.create(UrlRichMediaAsset.create(type, URL(url))))

    fun queryUserProfile() = messenger.queryUserProfile(request.event.senderId())

    override fun say(text: String): SayReaction {
        sendResponse(TextMessage.create(text))
        return SayReaction.create(text)
    }

    fun say(text: String, vararg inlineButtons: String) {
        say(text, inlineButtons = inlineButtons.map { PostbackButton.create(it, it) }.toTypedArray())
    }

    fun say(text: String, vararg inlineButtons: Button) {
        sendResponse(
            TemplateMessage.create(
                ButtonTemplate.create(text, inlineButtons.asList())
            )
        ).also {
            SayReaction.create(text)
            ButtonsReaction.create(inlineButtons.map { it.toReactionButton().text })
        }
    }

    fun buttons(title: String, buttons: List<String>): ButtonsReaction {
        val replies = buttons.map { TextQuickReply.create(it, it) }
        sendResponse(TextMessage.create(title, Optional.of(replies), Optional.empty()))
        return ButtonsReaction.create(buttons)
    }

    override fun image(url: String): ImageReaction {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.IMAGE)
        return ImageReaction.create(url)
    }

    fun video(url: String) {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.VIDEO)
    }

    override fun audio(url: String): AudioReaction {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.AUDIO)
        return AudioReaction.create(url)
    }

    fun file(url: String) {
        sendUrlRichMediaResponse(url, RichMediaAsset.Type.FILE)
    }

    fun carousel(first: CarouselElement, vararg elements: CarouselElement): CarouselReaction {
        val elementsList = listOf(first) + elements.toList()
        val template = GenericTemplate.create(elementsList.map { it.toTemplateElement() })
        sendResponse(TemplateMessage.create(template))
        return CarouselReaction.create("", elementsList.toCarouselReactionElements())
    }
}

private fun List<CarouselElement>.toCarouselReactionElements() = map {
    CarouselReaction.Element(
        title = it.title,
        buttons = it.buttons?.toReactionButtons() ?: emptyList(),
        description = it.subtitle,
        imageUrl = it.imageUrl?.toExternalForm()
    )
}

private fun List<Button>.toReactionButtons() = map { it.toReactionButton() }

private fun Button.toReactionButton() = when (this) {
    is CallButton -> CarouselReaction.Button(title(), "tel:${payload()}")
    is LogInButton -> CarouselReaction.Button(type().name, url().toExternalForm())
    is LogOutButton -> CarouselReaction.Button(type().name, "")
    is PostbackButton -> CarouselReaction.Button(title())
    is UrlButton -> CarouselReaction.Button(title(), url().toExternalForm())
    else -> CarouselReaction.Button(type().name)
}
