package com.justai.jaicf.channel.vk

import com.justai.jaicf.channel.vk.api.InMemoryVkContentStorage
import com.justai.jaicf.channel.vk.api.VkReactionsContentStorage
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.FileReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.logging.VideoReaction
import com.justai.jaicf.reactions.Reactions
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.docs.GetMessagesUploadServerType
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButton
import com.vk.api.sdk.objects.messages.KeyboardButtonAction
import com.vk.api.sdk.objects.messages.TemplateActionTypeNames
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

    val peerId: Int = request.clientId.toInt()

    private val messageTemplate get() = api.messages().send(actor).peerId(peerId).randomId(random.nextInt())

    companion object {
        private val random = Random()
    }

    override fun say(text: String): SayReaction {
        sendMessage(text, emptyList())
        return SayReaction.create(text)
    }

    fun say(text: String, vararg buttons: String) {
        sendMessage(
            text, buttons.map { buttonText ->
                KeyboardButton().apply {
                    action = KeyboardButtonAction().apply {
                        type = TemplateActionTypeNames.CALLBACK
                        label = buttonText
                        payload = "{}"
                    }
                }
            }.chunked(4) // we chunk because vk api allows max to 4 buttons in a single row
        )

        SayReaction.create(text)
        ButtonsReaction.create(buttons.asList())
    }

    override fun image(url: String): ImageReaction {
        messageTemplate.attachment(storage.getOrUploadImage(api, actor, peerId, url)).execute()
        return ImageReaction.create(url)
    }

    fun image(file: File): ImageReaction {
        messageTemplate.attachment(storage.getOrUploadImage(api, actor, peerId, file))
        return ImageReaction.create(file.absolutePath)
    }

    fun image(ownerId: Int, mediaId: Int, accessKey: Int? = null): ImageReaction {
        val imageId = when (accessKey) {
            null -> "photo${ownerId}_$mediaId"
            else -> "photo${ownerId}_${mediaId}_$accessKey"
        }
        messageTemplate.attachment(imageId).execute()
        return ImageReaction.create(imageId)
    }

    override fun audio(url: String): AudioReaction {
        val audio = storage.getOrUploadUrl(api, actor, peerId, url, GetMessagesUploadServerType.AUDIO_MESSAGE) {
            "doc${audioMessage.ownerId}_${audioMessage.id}"
        }
        messageTemplate.attachment(audio).execute()
        return AudioReaction.create(url)
    }

    fun audio(file: File): AudioReaction {
        val audio = storage.getOrUploadFile(api, actor, peerId, file, GetMessagesUploadServerType.AUDIO_MESSAGE) {
            "doc${audioMessage.ownerId}_${audioMessage.id}"
        }
        messageTemplate.attachment(audio).execute()
        return AudioReaction.create(file.absolutePath)
    }

    fun audio(ownerId: Int, mediaId: Int, accessKey: Int? = null): AudioReaction {
        val audioId = when (accessKey) {
            null -> "audio${ownerId}_$mediaId"
            else -> "audio${ownerId}_${mediaId}_$accessKey"
        }
        messageTemplate.attachment(audioId).execute()
        return AudioReaction.create(audioId)
    }

    fun video(ownerId: Int, mediaId: Int, accessKey: Int? = null): VideoReaction {
        val videoId = when (accessKey) {
            null -> "video${ownerId}_$mediaId"
            else -> "video${ownerId}_${mediaId}_$accessKey"
        }
        messageTemplate.attachment(videoId).execute()
        return VideoReaction.create(videoId)
    }

    fun document(file: File): FileReaction {
        val uploaded = storage.getOrUploadFile(api, actor, peerId, file, GetMessagesUploadServerType.DOC) { "doc${doc.ownerId}_${doc.id}" }
        messageTemplate.attachment(uploaded).execute()
        return FileReaction.create(file.absolutePath)
    }

    fun document(url: String): FileReaction {
        val uploaded = storage.getOrUploadUrl(api, actor, peerId, url, GetMessagesUploadServerType.DOC) { "doc${doc.ownerId}_${doc.id}" }
        messageTemplate.attachment(uploaded).execute()
        return FileReaction.create(url)
    }

    fun document(ownerId: Int, mediaId: Int, accessKey: Int? = null): FileReaction {
        val documentId = when (accessKey) {
            null -> "doc${ownerId}_$mediaId"
            else -> "doc${ownerId}_${mediaId}_$accessKey"
        }
        messageTemplate.attachment(documentId).execute()
        return FileReaction.create(documentId)
    }

    private fun sendMessage(text: String, buttons: List<List<KeyboardButton>>) =
        messageTemplate.message(text).keyboard(Keyboard().setButtons(buttons)).execute()
}
