package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Button
import com.justai.jaicf.channel.yandexalice.api.model.Image
import com.justai.jaicf.channel.yandexalice.api.model.ItemsList
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
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

    override fun say(text: String) {
        say(text, text)
    }

    fun say(text: String, tts: String) {
        builder.text += " $text"
        builder.tts += " $tts"
        SayReaction.register(text)
    }

    override fun buttons(vararg buttons: String) {
        buttons.forEach { buttons(Button(it, hide = true)) }
    }

    fun link(title: String, url: String) = buttons(Button(title, url = url))

    fun links(vararg links: Pair<String, String>) = links.forEach { link(it.first, it.second) }

    fun buttons(vararg buttons: Button) = builder.buttons.addAll(buttons).also {
        ButtonsReaction.register(buttons.asList().map { it.title })
    }

    override fun image(url: String) {
        image(Image(requireNotNull(api).getImageId(url)))
    }

    fun image(image: Image) {
        builder.card = image
        ImageReaction.register(image.imageId)
    }

    fun image(
        url: String,
        title: String? = null,
        description: String? = null,
        button: Button? = null
    ) = image(Image(requireNotNull(api).getImageId(url), title, description, button))

    fun itemsList(header: String? = null, footer: ItemsList.Footer? = null) =
        ItemsList(ItemsList.Header(header), footer).also { builder.card = it }

    override fun audio(id: String) {
        builder.tts += " <speaker audio='dialogs-upload/$skillId/$id.opus'>"
        AudioReaction.register(id)
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
