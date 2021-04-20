package com.justai.jaicf.channel.vk

import com.justai.jaicf.channel.vk.api.InMemoryVkContentStorage
import com.justai.jaicf.channel.vk.api.VkReactionsContentStorage
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.DocumentReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.vk.api.sdk.actions.Messages
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.enums.DocsType
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButton
import com.vk.api.sdk.objects.messages.KeyboardButtonAction
import com.vk.api.sdk.objects.messages.KeyboardButtonActionType
import java.io.File
import java.util.*

val Reactions.vk
    get() = this as? VkReactions


@Suppress("unused", "MemberVisibilityCanBePrivate")
class VkReactions(
    val api: VkApiClient,
    val actor: GroupActor,
    private val request: VkBotRequest,
    private val storage: VkReactionsContentStorage = InMemoryVkContentStorage
) : Reactions() {

    val messagesApi: Messages = api.messages()
    val peerId: Int = request.clientId.toInt()

    companion object {
        // extract to VK API
        private val random = Random()
    }

    override fun say(text: String) = sendMessage(text, emptyList()).let { SayReaction.create(text) }

    override fun image(url: String): ImageReaction {
        val vkPhoto = storage.getOrUploadImage(api, actor, peerId, url)
        api.messages().send(actor).attachment(vkPhoto).peerId(peerId).randomId(random.nextInt()).execute()
        return ImageReaction.create(url)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        // TODO: Async buttons with editing last message
        return super.buttons(*buttons)
    }

    override fun audio(url: String): AudioReaction {
        val uploaded = storage.getOrUploadUrl(api, actor, peerId, url, DocsType.DOC) { "doc${doc.ownerId}_${doc.id}" }
        api.messages().send(actor).attachment(uploaded).peerId(peerId).randomId(random.nextInt()).execute()
        return AudioReaction.create(url)
    }

    fun audio(file: File): AudioReaction {
        val uploaded = storage.getOrUploadFile(api, actor, peerId, file, DocsType.DOC) { "doc${doc.ownerId}_${doc.id}" }
        api.messages().send(actor).attachment(uploaded).peerId(peerId).randomId(random.nextInt()).execute()
        return AudioReaction.create(file.absolutePath)
    }

    fun vkAudio(vkAudioUrl: String): AudioReaction {
        api.messages().send(actor).attachment(vkAudioUrl).peerId(peerId).randomId(random.nextInt()).execute()
        return AudioReaction.create(vkAudioUrl)
    }

    fun image(file: File): ImageReaction {
        val vkPhoto = storage.getOrUploadImage(api, actor, peerId, file)
        api.messages().send(actor).attachment(vkPhoto).peerId(peerId).randomId(random.nextInt()).execute()
        return ImageReaction.create(file.absolutePath)
    }

    fun vkImage(vkImageUrl: String): ImageReaction {
        api.messages().send(actor).attachment(vkImageUrl).peerId(peerId).randomId(random.nextInt()).execute()
        return ImageReaction.create(vkImageUrl)
    }

    fun document(file: File): DocumentReaction {
        val uploaded = storage.getOrUploadFile(api, actor, peerId, file, DocsType.DOC) { "doc${doc.ownerId}_${doc.id}" }
        messagesApi.send(actor).attachment(uploaded).randomId(random.nextInt()).peerId(peerId).execute()
        return DocumentReaction.create(file.absolutePath)
    }

    fun document(url: String): DocumentReaction {
        val uploaded = storage.getOrUploadUrl(api, actor, peerId, url, DocsType.DOC) { "doc${doc.ownerId}_${doc.id}" }
        messagesApi.send(actor).attachment(url).randomId(random.nextInt()).peerId(peerId).execute()
        return DocumentReaction.create(url)
    }

    fun vkDocument(vkUrl: String): DocumentReaction {
        messagesApi.send(actor).attachment(vkUrl).randomId(random.nextInt()).peerId(peerId).execute()
        return DocumentReaction.create(vkUrl)
    }

    fun say(text: String, vararg buttons: String) {
        sendMessage(
            text, buttons.map { buttonText ->
                KeyboardButton().apply {
                    action = KeyboardButtonAction().apply {
                        type = KeyboardButtonActionType.TEXT
                        label = buttonText
                        payload = "{}"
                    }
                }
            }.chunked(4) // we chunk because vk api allows max to 4 buttons in a single row
        )

        SayReaction.create(text)
        ButtonsReaction.create(buttons.asList())
    }

    private fun sendMessage(text: String, buttons: List<List<KeyboardButton>>) =
        api.messages().send(actor)
            .message(text)
            .keyboard(Keyboard().setButtons(buttons)).peerId(request.message.peerId).randomId(random.nextInt())
            .peerId(request.message.peerId)
            .randomId(random.nextInt()).execute()
}
