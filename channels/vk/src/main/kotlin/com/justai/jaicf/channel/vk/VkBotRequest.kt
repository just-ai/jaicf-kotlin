package com.justai.jaicf.channel.vk

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.vk.api.sdk.objects.audio.Audio
import com.vk.api.sdk.objects.docs.Doc
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

    fun getRequestForMessage(message: Message): VkBotRequest {
        if (message.text.isNotEmpty()) {
            return VkQueryBotRequest(message)
        }

        customConverters.forEach { converter ->
            converter(message)?.let { return it }
        }

        val request = when {
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
            else -> null
        }


        return requireNotNull(request)
    }

    fun registerConverter(converter: VKMessageConverter) = customConverters.add(converter)
}

data class VkQueryBotRequest(
    override val message: Message
) : VkBotRequest, QueryBotRequest(
    clientId = message.clientId,
    input = message.text
)

data class VkMultipleAttachments(
    override val message: Message,
    val audios: List<Audio> = message.attachments.mapNotNull { it.audio },
    val documents: List<Doc> = message.attachments.mapNotNull { it.doc },
    val photos: List<Photo> = message.attachments.mapNotNull { it.photo },
    val files: List<Video> = message.attachments.mapNotNull { it.video }
) : VkBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.ATTACHMENTS
)

data class VkAudioBotRequest(
    override val message: Message,
    val audios: List<Audio> = message.audios
) : VkBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.AUDIO
)

data class VkDocumentBotRequest(
    override val message: Message,
    val documents: List<Doc> = message.documents
) : VkBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.DOCUMENT
)

data class VkPhotoBotRequest(
    override val message: Message,
    val documents: List<Photo> = message.photos
) : VkBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.PHOTO
)

data class VkVideoBotRequest(
    override val message: Message,
    val documents: List<Video> = message.videos
) : VkBotRequest, EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.VIDEO
)

private val Message.clientId get() = peerId.toString()

private fun Message.hasAudio() = audios.any()
private val Message.audios get() = attachments.mapNotNull { it.audio }

private fun Message.hasDocument() = documents.any()
private val Message.documents get() = attachments.mapNotNull { it.doc }

private fun Message.hasPhoto() = photos.any()
private val Message.photos get() = attachments.mapNotNull { it.photo }

private fun Message.hasVideo() = videos.any()
private val Message.videos get() = attachments.mapNotNull { it.video }

private fun Message.hasOtherExcept(type: MessageAttachmentType) = attachments.any { it.type != type }