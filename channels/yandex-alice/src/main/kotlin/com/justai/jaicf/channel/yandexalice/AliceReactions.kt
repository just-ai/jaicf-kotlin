package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Button
import com.justai.jaicf.channel.yandexalice.api.model.Image
import com.justai.jaicf.channel.yandexalice.api.model.ImageGallery
import com.justai.jaicf.channel.yandexalice.api.model.ItemsList
import com.justai.jaicf.logging.*
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

val Reactions.alice
    get() = this as? AliceReactions

class AliceReactions(
    val api: AliceApi?,
    request: AliceBotRequest,
    override val response: AliceBotResponse
) : ResponseReactions<AliceBotResponse>(response) {

    private val builder = response.response ?: AliceBotResponse.Response()
    private val skillId = request.session.skillId

    override fun say(text: String): SayReaction {
        return say(text, text)
    }

    fun say(text: String, tts: String): SayReaction {
        builder.text += " $text"
        builder.tts += " $tts"
        return SayReaction.create(text)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        return buttons(*buttons.map { Button(it, hide = true) }.toTypedArray())
    }

    fun link(title: String, url: String) = buttons(Button(title, url = url))

    fun links(vararg links: Pair<String, String>) = links.forEach { link(it.first, it.second) }

    fun buttons(vararg buttons: Button): ButtonsReaction {
        builder.buttons.addAll(buttons)
        return ButtonsReaction.create(buttons.asList().map { it.title })
    }

    override fun image(url: String): ImageReaction {
        return image(Image(requireNotNull(api).getImageId(url)))
    }

    fun image(image: Image): ImageReaction {
        builder.card = image
        return ImageReaction.create(image.imageId)
    }

    fun image(
        url: String,
        title: String? = null,
        description: String? = null,
        button: Button? = null
    ) = image(Image(requireNotNull(api).getImageId(url), title, description, button))

    /**
     * Sends a message with a list of images and optionaly [Header] and [Footer].
     * A list must contain from 1 to 5 images. [Image.button] is ignored.
     *
     * @see <a href="https://yandex.ru/dev/dialogs/alice/doc/protocol.html#ariaid-title3">yandex-protocol</a>
     */
    fun itemsList(header: String? = null, footer: ItemsList.Footer? = null) =
        ItemsList(ItemsList.Header(header), footer).also { builder.card = it }

    /**
     * Sends a message with a carousel of images.
     * A carousel must contain from 1 to 7 images. [Image.button] is ignored.
     *
     * @see <a href="https://yandex.ru/dev/dialogs/alice/doc/protocol.html#ariaid-title3">yandex-protocol</a>
     */
    fun imageGallery(first: Image, vararg images: Image): CarouselReaction {
        val imageList = listOf(first) + images.toList()
        builder.card = ImageGallery(imageList)
        return CarouselReaction.create("", mapImagesToCarouselReactionElements(imageList))
    }

    override fun audio(id: String): AudioReaction {
        builder.tts += " <speaker audio='dialogs-upload/$skillId/$id.opus'>"
        return AudioReaction.create(id)
    }

    fun endSession(): EndSessionReaction {
        builder.endSession = true
        return EndSessionReaction.create()
    }

    fun startAccountLinking() {
        response.response = null
        response.startAccountLinking = JsonObject(mapOf())
    }

    fun sessionState(state: JsonObject) {
        response.sessionState = state
    }

    fun updateUserState(key: String, value: JsonElement?) {
        response.userStateUpdate[key] = value
    }

    fun deleteFromUserState(key: String) {
        response.userStateUpdate[key] = null
    }
}

private fun AliceReactions.mapImagesToCarouselReactionElements(imageList: List<Image>) = imageList.map {
    CarouselReaction.Element(
        title = it.title ?: "",
        buttons = emptyList(),
        description = it.description,
        imageUrl = api?.getImageUrl(it.imageId)
    )
}
