package com.justai.jaicf.channel.vk

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.vk.api.sdk.objects.Validable
import com.vk.api.sdk.objects.audio.Audio
import com.vk.api.sdk.objects.docs.Doc
import com.vk.api.sdk.objects.messages.Message
import com.vk.api.sdk.objects.messages.MessageAttachment
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.video.Video
import com.vk.api.sdk.objects.wall.Wallpost
import com.vk.api.sdk.objects.wall.WallpostAttachment

@Suppress("unused")
interface VkBotRequest

data class VkTextBotRequest(
    val message: Message,
    val attachments: List<MessageAttachment> = message.attachments ?: emptyList()
) : QueryBotRequest(
    clientId = message.clientId,
    input = message.text
)

data class VkWallPostBotRequest(
    val wallPost: Wallpost,
    val attachments: List<WallpostAttachment> = wallPost.attachments ?: emptyList()
) : QueryBotRequest(
    clientId = wallPost.clientId,
    input = wallPost.text
)

// when text is null and only audio is sent
data class VkAttachmentsEvent(
    val message: Message,
    val audios: List<Audio> = message.attachments.mapNotNull { it.audio },
    val documents: List<Doc> = message.attachments.mapNotNull { it.doc },
    val photos: List<Photo> = message.attachments.mapNotNull { it.photo },
    val files: List<Video> = message.attachments.mapNotNull { it.video }
) : EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.ATTACHMENTS
)

// when text is null and only audio is sent
data class VkAudioBotRequest(
    val message: Message,
    val documents: List<Doc> = message.attachments.mapNotNull { it.doc }
) : EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.AUDIO
)

data class VkDocumentBotRequest(
    val message: Message,
    val documents: List<Doc> = message.attachments.mapNotNull { it.doc }
) : EventBotRequest(
    clientId = message.clientId,
    input = VkEvent.AUDIO
)

data class VkGroupEvent(
    private val data: Validable,
    val event: String
) : EventBotRequest(
    clientId = "some-client-id",
    input = event
)

private val Message.clientId get() = peerId.toString()
private val Wallpost.clientId get() = fromId.toString()