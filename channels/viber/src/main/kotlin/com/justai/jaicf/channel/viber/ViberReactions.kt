package com.justai.jaicf.channel.viber

import com.justai.jaicf.channel.viber.api.Carousel
import com.justai.jaicf.channel.viber.api.CarouselElement
import com.justai.jaicf.channel.viber.api.toRichMediaObject
import com.justai.jaicf.channel.viber.reaction.StickerReaction
import com.justai.jaicf.channel.viber.sdk.api.FunctionalButton
import com.justai.jaicf.channel.viber.sdk.api.KeyboardBuilder
import com.justai.jaicf.channel.viber.sdk.api.ReplyButton
import com.justai.jaicf.channel.viber.sdk.api.ViberApi
import com.justai.jaicf.channel.viber.sdk.api.ViberButton
import com.justai.jaicf.channel.viber.sdk.api.toKeyboard
import com.justai.jaicf.channel.viber.sdk.api.toRichMediaObject
import com.justai.jaicf.channel.viber.sdk.message.FileMessage
import com.justai.jaicf.channel.viber.sdk.message.KeyboardMessage
import com.justai.jaicf.channel.viber.sdk.message.Location
import com.justai.jaicf.channel.viber.sdk.message.LocationMessage
import com.justai.jaicf.channel.viber.sdk.message.Message
import com.justai.jaicf.channel.viber.sdk.message.PictureMessage
import com.justai.jaicf.channel.viber.sdk.message.RichMediaMessage
import com.justai.jaicf.channel.viber.sdk.message.RichMediaObject
import com.justai.jaicf.channel.viber.sdk.message.StickerMessage
import com.justai.jaicf.channel.viber.sdk.message.TextMessage
import com.justai.jaicf.channel.viber.sdk.message.UrlMessage
import com.justai.jaicf.channel.viber.sdk.message.VideoMessage
import com.justai.jaicf.channel.viber.sdk.profile.BotProfile
import com.justai.jaicf.channel.viber.sdk.profile.UserProfile
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.CarouselReaction
import com.justai.jaicf.logging.FileReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.LocationReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.logging.UrlReaction
import com.justai.jaicf.logging.VideoReaction
import com.justai.jaicf.logging.currentState
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
class ViberReactions internal constructor(
    val sender: BotProfile,
    val receiver: UserProfile,
    val viberApi: ViberApi,
    val authToken: String
) : Reactions(), JaicpCompatibleAsyncReactions {

    override fun say(text: String): SayReaction {
        sendMessage(TextMessage(text))
        return SayReaction.create(text)
    }

    fun say(text: Any): SayReaction {
        sendMessage(TextMessage(text.toString()))
        return SayReaction.create(text.toString())
    }

    override fun image(url: String): ImageReaction {
        return image(url, null)
    }

    /**
     * @link https://developers.viber.com/docs/api/rest-bot-api/#picture-message
     */
    fun image(url: String, description: String? = null): ImageReaction {
        sendMessage(PictureMessage(url, description = description, thumbnail = url))
        return ImageReaction.create(url)
    }

    override fun buttons(vararg buttons: String) = keyboard(buttons.map { ReplyButton(it) })

    fun keyboard(
        defaultStyle: ViberButton.Style = keyboardDefaultStyle,
        builder: KeyboardBuilder.() -> Unit
    ): ButtonsReaction {
        val viberKeyboard = KeyboardBuilder(defaultStyle).apply(builder).build()
        sendMessage(KeyboardMessage(keyboard = viberKeyboard.toKeyboard()))
        return ButtonsReaction.create(viberKeyboard.buttons.map { it.text ?: "" })
    }

    fun keyboard(vararg buttons: FunctionalButton): ButtonsReaction {
        return keyboard(buttons.toList())
    }

    fun keyboard(buttons: List<FunctionalButton>): ButtonsReaction {
        sendMessage(KeyboardMessage(keyboard = buttons.toKeyboard()))
        return ButtonsReaction.create(buttons.map { it.text })
    }

    fun inlineButtons(
        defaultStyle: ViberButton.Style = inlineButtonsDefaultStyle,
        builder: KeyboardBuilder.() -> Unit
    ) {
        val viberKeyboard = KeyboardBuilder(defaultStyle).apply(builder).build()
        richObject(viberKeyboard.toRichMediaObject(), "_")
    }

    fun file(url: String, file: File): FileReaction {
        return file(url, file.nameWithoutExtension, file.extension)
    }

    /**
     * Not allowed animated gif, exe, etc.
     * @link https://developers.viber.com/docs/api/rest-bot-api/#forbiddenFileFormats
     */
    fun file(url: String, filename: String, extension: String): FileReaction {
        val size = getFileSize(url)
        sendMessage(FileMessage(url, size, "$filename.$extension"))
        return FileReaction.create(url)
    }

    fun location(latitude: Double, longitude: Double): LocationReaction {
        sendMessage(LocationMessage(Location(latitude, longitude)))
        return LocationReaction.create(latitude.toFloat(), longitude.toFloat())
    }

    /**
     * @link https://developers.viber.com/docs/tools/sticker-ids/
     */
    fun sticker(stickerId: Int): StickerReaction {
        sendMessage(StickerMessage(stickerId))
        return StickerReaction(stickerId.toLong(), currentState)
    }

    fun url(url: String): UrlReaction {
        sendMessage(UrlMessage(url))
        return UrlReaction.create(url)
    }

    fun video(url: String, text: String? = null): VideoReaction {
        sendMessage(VideoMessage(url, getFileSize(url), text = text, thumbnail = url))
        return VideoReaction.create(url)
    }

    fun carousel(vararg elements: CarouselElement): CarouselReaction {
        val carousel = Carousel(elements.toList())
        return carousel(carousel)
    }

    fun carousel(carousel: Carousel): CarouselReaction {
        sendMessage(RichMediaMessage(carousel.toRichMediaObject(), "carousel"))
        return CarouselReaction.create("", carousel.elements.toCarouselReactionElements())
    }

    fun richObject(richMediaObject: RichMediaObject, alternativeText: String) {
        sendMessage(RichMediaMessage(richMediaObject, alternativeText))
    }

    fun sendMessage(message: Message) {
        viberApi.sendMessage(sender, receiver, message, authToken)
    }

    companion object {
        var keyboardDefaultStyle: ViberButton.Style = ViberButton.Style(backgroundColor = "#aed6f1")
        var inlineButtonsDefaultStyle: ViberButton.Style = ViberButton.Style(backgroundColor = "#fdebd0")
    }
}

private fun List<CarouselElement>.toCarouselReactionElements() = map {
    CarouselReaction.Element(
        title = it.title,
        buttons = it.button?.toReactionButtonsList() ?: emptyList(),
        description = it.subtitle,
        imageUrl = it.imageUrl
    )
}

private fun ViberButton.toReactionButtonsList() = listOf(CarouselReaction.Button(text, redirectUrl))

private val client = HttpClient {
    HttpTimeout(2000, 2000, 2000)
}

private fun getFileSize(url: String): Int {
    return try {
        runBlocking(Dispatchers.IO) {
            val response: HttpResponse = client.head(url)
            response.contentLength()?.toInt() ?: 1
        }
    } catch (e: Exception) {
        1
    }
}
