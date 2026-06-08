package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.max.dto.BotAddedUpdate
import com.justai.jaicf.channel.max.dto.BotRemovedUpdate
import com.justai.jaicf.channel.max.dto.BotStartedUpdate
import com.justai.jaicf.channel.max.dto.MaxAudioAttachment
import com.justai.jaicf.channel.max.dto.MaxContactAttachment
import com.justai.jaicf.channel.max.dto.MaxMessage
import com.justai.jaicf.channel.max.dto.MaxUpdate
import com.justai.jaicf.channel.max.dto.MessageCallbackUpdate
import com.justai.jaicf.channel.max.dto.MessageCreatedUpdate

/**
 * Stateless mapping from a parsed [MaxUpdate] to a [MaxBotRequest].
 * Detection order for created messages is attachment-first (contact, audio) then text,
 * so an attachment carrying a caption is not misclassified as plain text.
 */
object MaxBotRequestFactory {

    fun create(update: MaxUpdate): MaxBotRequest? = when (update) {
        is MessageCreatedUpdate -> fromMessage(update.message)
        is MessageCallbackUpdate -> update.message?.let { MaxQueryRequest(update.callback, it) }
        is BotAddedUpdate -> MaxBotAddedRequest(update)
        is BotStartedUpdate -> MaxBotStartedRequest(update)
        is BotRemovedUpdate -> MaxBotRemovedRequest(update)
        else -> null
    }

    private fun fromMessage(message: MaxMessage): MaxBotRequest? {
        val attachments = message.body.attachments.orEmpty()
        return when {
            attachments.any { it is MaxContactAttachment } -> MaxContactRequest(message)
            attachments.any { it is MaxAudioAttachment } -> MaxAudioRequest(message)
            !message.body.text.isNullOrBlank() -> MaxTextRequest(message)
            else -> null
        }
    }
}
