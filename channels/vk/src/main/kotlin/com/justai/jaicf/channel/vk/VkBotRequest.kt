package com.justai.jaicf.channel.vk

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.vk.api.sdk.objects.audio.Audio
import com.vk.api.sdk.objects.base.Geo
import com.vk.api.sdk.objects.base.Sticker
import com.vk.api.sdk.objects.docs.Doc
import com.vk.api.sdk.objects.messages.AudioMessage
import com.vk.api.sdk.objects.messages.Message
import com.vk.api.sdk.objects.messages.MessageAttachment
import com.vk.api.sdk.objects.messages.MessageAttachmentType
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.video.Video

@Suppress("unused")
interface VkBotRequest : BotRequest {
    val message: Message
    val attachments: List<MessageAttachment>
        get() = message.attachments ?: emptyList()
}

typealias VKMessageConverter = (Message) -> VkBotRequest?

object VkBotRequestFactory {

    private val customConverters: MutableList<VKMessageConverter> = mutableListOf()

    fun getRequestForMessage(message: Message): VkBotRequest? {
        if (message.text.isNotEmpty()) {
            return VkQueryBotRequest(message)
        }

        customConverters.forEach { converter ->
            converter(message)?.let { return it }
        }

        message.geo?.let { return VkLocationRequest(message) }

        return when {
            message.hasAudio() -> {
                if (message.hasOtherExcept(MessageAttachmentType.AUDIO)) VkMultipleAttachments(message)
                else VkAudioBotRequest(message)
            }
            message.hasDocument() -> {
                if (message.hasOtherExcept(MessageAttachmentType.DOC)) VkMultipleAttachments(message)
                else VkDocumentBotRequest(message)
            }
            message.hasPhoto() -> {
                if (message.hasOtherExcept(MessageAttachmentType.PHOTO)) VkMultipleAttachments(message)
                else VkPhotoBotRequest(message)

            }
            message.hasVideo() -> {
                if (message.hasOtherExcept(MessageAttachmentType.VIDEO)) VkMultipleAttachments(message)
                else VkVideoBotRequest(message)
            }
            message.hasSticker() -> VkStickerRequest(message)
            message.hasAudioMessage() -> VkAudioMessageRequest(message)
            else -> null
        }
    }

    fun registerConverter(converter: VKMessageConverter) = customConverters.add(converter)
}

data class VkQueryBotRequest(
    override val message: Message
) : VkBotRequest, QueryBotRequest(message.clientId, message.text)

data class VkAudioBotRequest(
    override val message: Message,
    val audios: List<Audio> = message.audios
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.AUDIO)

data class VkDocumentBotRequest(
    override val message: Message,
    val documents: List<Doc> = message.documents
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.DOCUMENT)

data class VkPhotoBotRequest(
    override val message: Message,
    val photos: List<Photo> = message.photos
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.PHOTO)

data class VkVideoBotRequest(
    override val message: Message,
    val videos: List<Video> = message.videos
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.VIDEO)

data class VkLocationRequest(
    override val message: Message,
    val geo: Geo = message.geo
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.LOCATION)

data class VkStickerRequest(
    override val message: Message,
    val stickers: List<Sticker> = message.stickers
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.STICKER)

data class VkAudioMessageRequest(
    override val message: Message,
    val audioMessages: List<AudioMessage> = message.audioMessage
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.AUDIO_MESSAGE)

data class VkMultipleAttachments(
    override val message: Message,
    val audios: List<Audio> = message.attachments.mapNotNull { it.audio },
    val documents: List<Doc> = message.attachments.mapNotNull { it.doc },
    val photos: List<Photo> = message.attachments.mapNotNull { it.photo },
    val files: List<Video> = message.attachments.mapNotNull { it.video }
) : VkBotRequest, EventBotRequest(message.clientId, VkEvent.MULTIPLE_ATTACHMENTS)

private val Message.clientId get() = peerId.toString()

private fun Message.hasAudio() = audios.any()
private val Message.audios get() = attachments.mapNotNull { it.audio }

private fun Message.hasDocument() = documents.any()
private val Message.documents get() = attachments.mapNotNull { it.doc }

private fun Message.hasPhoto() = photos.any()
private val Message.photos get() = attachments.mapNotNull { it.photo }

private fun Message.hasVideo() = videos.any()
private val Message.videos get() = attachments.mapNotNull { it.video }

private fun Message.hasSticker() = stickers.any()
private val Message.stickers get() = attachments.mapNotNull { it.sticker }

private fun Message.hasAudioMessage() = audioMessage.any()
private val Message.audioMessage get() = attachments.mapNotNull { it.audioMessage }

private fun Message.hasOtherExcept(type: MessageAttachmentType) = attachments.any { it.type != type }