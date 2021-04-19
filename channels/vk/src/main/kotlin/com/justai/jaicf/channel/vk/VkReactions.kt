package com.justai.jaicf.channel.vk

import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.vk.api.sdk.actions.Messages
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButton
import com.vk.api.sdk.objects.messages.KeyboardButtonAction
import com.vk.api.sdk.objects.messages.KeyboardButtonActionType
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap

val Reactions.vk
    get() = this as? VkReactions

private val httpClient = HttpClientBuilder.create().build()

@Suppress("unused", "MemberVisibilityCanBePrivate")
class VkReactions(
    private val vk: VkApiClient,
    private val groupActor: GroupActor,
    private val request: VkBotRequest
) : Reactions() {

    val api: Messages = vk.messages()
    val peerId: Int = request.clientId.toInt()

    companion object {
        // extract to VK API
        private val random = Random()
        private val imagesMap = ConcurrentHashMap<String, String>()
    }

    override fun say(text: String) = sendMessage(text, emptyList()).let { SayReaction.create(text) }

    override fun image(url: String): ImageReaction {
        val vkPhoto = imagesMap.computeIfAbsent(url, this::uploadPhoto)
        vk.messages().send(groupActor).attachment(vkPhoto).peerId(peerId).randomId(random.nextInt()).execute()
        return ImageReaction.create(url)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        // TODO: Async buttons with editing last message
        return super.buttons(*buttons)
    }

    override fun audio(url: String): AudioReaction {
        return super.audio(url)
    }

    fun audio(file: File) {}

    fun vkAudio(vkAudioUrl: String) {}

    fun image(file: File) {}

    fun vkImage(vkImageUrl: String) {}

    fun document(file: File) {}

    fun document(url: String) {}

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
        vk.messages().send(groupActor)
            .message(text)
            .keyboard(Keyboard().setButtons(buttons)).peerId(request.message.peerId).randomId(random.nextInt())
            .peerId(request.message.peerId)
            .randomId(random.nextInt()).execute()

    private fun uploadPhoto(url: String): String {
        val ext = FilenameUtils.getExtension(url).orIfEmpty("jpeg")
        val file = File.createTempFile("image", "vk_upload.$ext")
        httpClient.execute(HttpGet(URI(url))).use {
            file.writeBytes(it.entity.content.readBytes())
        }
        return uploadPhoto(file)
    }

    private fun uploadPhoto(file: File): String {
        val uploadUrl = vk.photos().getMessagesUploadServer(groupActor).execute().uploadUrl.toString()
        val uploadResponse = vk.upload().photoMessage(uploadUrl, file).execute()
        val photo = vk.photos().saveMessagesPhoto(groupActor, uploadResponse.photo)
            .server(uploadResponse.server)
            .hash(uploadResponse.hash).execute()
            .first()
        return "photo${photo.ownerId}_${photo.id}"
    }
}

private fun String.orIfEmpty(other: String) = if (isNullOrEmpty()) other else this