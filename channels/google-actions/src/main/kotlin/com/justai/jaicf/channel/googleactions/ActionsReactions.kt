package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionRequest
import com.google.actions.api.Capability
import com.google.api.services.actions_fulfillment.v2.model.*
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.*

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

    private var simpleResponse: SimpleResponse? = null

    private fun withCapability(capability: Capability, block: () -> Unit) {
        if (request.hasCapability(capability.value)) {
            block.invoke()
        }
    }

    private fun clean(text: String) = text.replace(ssmlRegex, " ")

    override fun say(text: String): SayReaction {
        addSimpleResponse(clean(text), text)
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

    override fun image(url: String): ImageReaction {
        response.builder.add(Image().setUrl(url))
        simpleResponse = null
        return ImageReaction.create(url)
    }

    fun endConversation() = response.builder.endConversation()

    fun addSimpleResponse(displayText: String, ssml: String = displayText) {
        val sr = simpleResponse ?: SimpleResponse()
        val fixedSsml = sr.ssml?.let {
            it.substring(7, it.length - 8)
        }

        if (simpleResponse == null) {
            response.builder.add(sr)
            simpleResponse = sr
        }

        sr.displayText = sr.displayText?.plus(displayText) ?: displayText
        sr.ssml = "<speak>" + (fixedSsml?.plus(" $ssml ") ?: ssml) + "</speak>"
    }

    fun playAudio(
        url: String,
        name: String? = null,
        description: String? = null,
        icon: Image? = null,
        largeImage: Image? = null,
        vararg buttons: String
    ) = withCapability(Capability.MEDIA_RESPONSE_AUDIO) {
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
    }.also {
        AudioReaction.create(url)
    }
}