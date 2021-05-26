package com.justai.jaicf.channel.googleactions

import com.google.actions.api.ActionRequest
import com.google.actions.api.ActionResponse
import com.google.actions.api.Capability
import com.google.api.services.actions_fulfillment.v2.model.Image
import com.google.api.services.actions_fulfillment.v2.model.MediaObject
import com.google.api.services.actions_fulfillment.v2.model.MediaResponse
import com.google.api.services.actions_fulfillment.v2.model.RichResponseItem
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse
import com.google.api.services.actions_fulfillment.v2.model.Suggestion
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val Reactions.actions
    get() = this as? ActionsReactions

private val logger: Logger = LoggerFactory.getLogger("ActionsFulfillment")

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

internal fun ActionResponse.withInternalRestructuring() = apply {
    richResponse?.items?.run {
        val textResponseItem = concatenateTextResponses() ?: run {
            logger.warn("Response must contain at least one text item. Use reactions.say() to generate any.")
            RichResponseItem().setSimpleResponse(SimpleResponse().setDisplayText(" ").setSsml(" "))
        }

        val originalRichItems = filter { item -> item.simpleResponse == null }
        if (originalRichItems.size > 1) {
            logger.warn("Response can contain only one image, audio or other rich media item")
        }

        val hasMedia = any { item -> item.mediaResponse != null }
        val hasSuggestions = richResponse?.suggestions?.any() ?: false
        if (hasMedia && !hasSuggestions) {
            logger.warn("Response with media items must contain buttons. Use reactions.buttons() to generate any.")
        }

        richResponse?.items = listOf(textResponseItem) + originalRichItems
        webhookResponse?.fulfillmentText = textResponseItem.simpleResponse.displayText
    }
}

internal fun List<RichResponseItem>.concatenateTextResponses(): RichResponseItem? =
    mapNotNull { item -> item.simpleResponse }
        .ifEmpty { return null }
        .fold(SimpleResponse()) { acc, response ->
            acc.apply {
                displayText = displayText.joinNullable(response.displayText, "  \n")
                ssml = ssml.joinNullable(response.ssml, " ")
                textToSpeech = textToSpeech.joinNullable(response.textToSpeech, " ")
            }
        }
        .apply { ssml?.let { ssml = "<speak>$ssml</speak>" } }
        .let(RichResponseItem()::setSimpleResponse)

private fun String?.joinNullable(other: String?, separator: String): String? {
    if (this == null && other == null) return null

    val left = this ?: return other
    val right = other ?: return left

    return left + separator + right
}