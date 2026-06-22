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
import com.justai.jaicf.channel.max.dto.UnknownMaxUpdate

/**
 * Maps a parsed [MaxUpdate] to the matching [MaxBotRequest], or `null` for updates this
 * channel does not handle. Pure and stateless. For created messages, attachments are
 * detected before text so a captioned attachment is never misread as a plain text message.
 */
internal fun MaxUpdate.toBotRequest(): MaxBotRequest? = when (this) {
    is MessageCreatedUpdate -> message.toBotRequest()
    is MessageCallbackUpdate -> message?.let { MaxCallbackRequest(callback, it) }
    is BotAddedUpdate -> MaxBotAddedRequest(this)
    is BotStartedUpdate -> MaxBotStartedRequest(this)
    is BotRemovedUpdate -> MaxBotRemovedRequest(this)
    is UnknownMaxUpdate -> null
}

private fun MaxMessage.toBotRequest(): MaxBotRequest? {
    val attachments = body.attachments.orEmpty()
    return when {
        attachments.any { it is MaxContactAttachment } -> MaxContactRequest(this)
        attachments.any { it is MaxAudioAttachment } -> MaxAudioRequest(this)
        !body.text.isNullOrBlank() -> MaxTextRequest(this)
        else -> null
    }
}
