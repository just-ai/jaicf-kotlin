package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Button
import com.justai.jaicf.channel.yandexalice.api.model.Image
import com.justai.jaicf.channel.yandexalice.api.model.ItemsList
import com.justai.jaicf.reactions.*
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
        say(text, text)
        return SayReaction.create(text)
    }

    fun say(text: String, tts: String) {
        builder.text += " $text"
        builder.tts += " $tts"
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        buttons.forEach { buttons(Button(it, hide = true)) }
        return ButtonsReaction.create(buttons.asList())
    }

    fun link(title: String, url: String) = buttons(Button(title, url = url))

    fun links(vararg links: Pair<String, String>) = links.forEach { link(it.first, it.second) }

    fun buttons(vararg buttons: Button) = builder.buttons.addAll(buttons)

    override fun image(url: String): ImageReaction {
        builder.card = Image(requireNotNull(api).getImageId(url))
        return ImageReaction.create(url)
    }

    fun image(image: Image) {
        builder.card = image
        ImageReaction.create(image.imageId)
    }

    fun image(
        url: String,
        title: String? = null,
        description: String? = null,
        button: Button? = null
    ) = Image(requireNotNull(api).getImageId(url), title, description, button).also { builder.card = it }.also {
        ImageReaction.create(url)
    }

    fun itemsList(header: String? = null, footer: ItemsList.Footer? = null) =
        ItemsList(ItemsList.Header(header), footer).also { builder.card = it }

    override fun audio(id: String): AudioReaction {
        builder.tts += " <speaker audio='dialogs-upload/$skillId/$id.opus'>"
        return AudioReaction.create(id)
    }

    fun endSession() {
        builder.endSession = true
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
