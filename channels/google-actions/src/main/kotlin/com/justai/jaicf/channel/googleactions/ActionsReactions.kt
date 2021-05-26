package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionRequest
import com.google.actions.api.Capability
import com.google.api.services.actions_fulfillment.v2.model.Image
import com.google.api.services.actions_fulfillment.v2.model.MediaObject
import com.google.api.services.actions_fulfillment.v2.model.MediaResponse
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse
import com.google.api.services.actions_fulfillment.v2.model.Suggestion
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions

val Reactions.actions
    get() = this as? ActionsReactions

class ActionsReactions(
    private val request: ActionRequest,
    override val response: ActionsBotResponse
) : ResponseReactions<ActionsBotResponse>(response) {

    companion object {
        private val ssmlRegex = "<.*?>".toRegex()
    }

    val userStorage = response.builder.userStorage

    private fun withCapability(capability: Capability, block: () -> Unit) {
        if (request.hasCapability(capability.value)) {
            block.invoke()
        }
    }

    private fun clean(text: String) = text.replace(ssmlRegex, " ")

    override fun say(text: String): SayReaction {
        val simpleResponse = SimpleResponse().also {
            it.displayText = text
            it.ssml = clean(text)
        }

        response.builder.add(simpleResponse)
        return SayReaction.create(text)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        buttons.forEach { title ->
            response.builder.add(Suggestion().also {
                it.title = title
            })
        }
        return ButtonsReaction.create(buttons.asList())
    }

    override fun image(url: String) = image(url, null)

    @Suppress("MemberVisibilityCanBePrivate")
    fun image(url: String, accessibilityText: String?): ImageReaction {
        val image = Image().setUrl(url)
        accessibilityText?.let(image::setAccessibilityText)

        response.builder.add(image)
        return ImageReaction.create(url)
    }

    fun endConversation() = response.builder.endConversation()

    fun playAudio(
        url: String,
        name: String? = null,
        description: String? = null,
        icon: Image? = null,
        largeImage: Image? = null,
        buttons: List<String>
    ) = withCapability(Capability.MEDIA_RESPONSE_AUDIO) {
        require(buttons.isNotEmpty())

        response.builder
            .add(MediaResponse().also { res ->
                res.mediaType = "AUDIO"
                res.mediaObjects = listOf(
                    MediaObject().also {
                        it.name = name
                        it.description = description
                        it.icon = icon
                        it.largeImage = largeImage
                        it.contentUrl = url
                    }
                )
            })

        AudioReaction.create(url)
        buttons(*buttons.toTypedArray())
    }
}
