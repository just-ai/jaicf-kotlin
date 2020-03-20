package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.channel.yandexalice.api.AliceApi
import com.justai.jaicf.channel.yandexalice.api.AliceBotRequest
import com.justai.jaicf.channel.yandexalice.api.AliceBotResponse
import com.justai.jaicf.channel.yandexalice.api.model.Button
import com.justai.jaicf.channel.yandexalice.api.model.Image
import com.justai.jaicf.channel.yandexalice.api.model.ItemsList
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions

val Reactions.alice
    get() = this as? AliceReactions

class AliceReactions(
    val api: AliceApi?,
    request: AliceBotRequest,
    override val response: AliceBotResponse
) : ResponseReactions<AliceBotResponse>(response) {

    private val builder = response.response
    private val skillId = request.session.skillId

    override fun say(text: String) = say(text, text)

    fun say(text: String, tts: String) {
        builder.text += " $text"
        builder.tts += " $tts"
    }

    override fun buttons(vararg buttons: String)
            = buttons.forEach { buttons(Button(it, hide = true)) }

    fun link(title: String, url: String)
            = buttons(Button(title, url = url))

    fun links(vararg links: Pair<String, String>)
            = links.forEach { link(it.first, it.second) }

    fun buttons(vararg buttons: Button)
            = builder.buttons.addAll(buttons)

    override fun image(url: String) {
        builder.card = Image(requireNotNull(api).getImageId(url))
    }

    fun image(image: Image) {
        builder.card = image
    }

    fun image(
        url: String,
        title: String? = null,
        description: String? = null,
        button: Button? = null
    ) = Image(requireNotNull(api).getImageId(url), title, description, button).also { builder.card = it }

    fun itemsList(header: String? = null, footer: ItemsList.Footer? = null)
            = ItemsList(ItemsList.Header(header), footer).also { builder.card = it }

    fun audio(id: String) {
        builder.tts += " <speaker audio='dialogs-upload/$skillId/$id.opus'>"
    }

    fun endSession() {
        builder.endSession = true
    }
}