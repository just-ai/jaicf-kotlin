package com.justai.jaicf.channel.max.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

data class NewMessageBody(
    val text: String? = null,
    val attachments: List<MaxAttachmentRequest>? = null,
    val format: String? = null,          // "markdown" | "html"
    val notify: Boolean? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MaxAttachmentRequest.InlineKeyboard::class, name = "inline_keyboard"),
    JsonSubTypes.Type(value = MaxAttachmentRequest.Audio::class, name = "audio"),
    JsonSubTypes.Type(value = MaxAttachmentRequest.Image::class, name = "image")
)
sealed class MaxAttachmentRequest {
    @JsonTypeName("inline_keyboard")
    data class InlineKeyboard(val payload: MaxKeyboardPayload) : MaxAttachmentRequest()
    @JsonTypeName("audio")
    data class Audio(val payload: MaxMediaToken) : MaxAttachmentRequest()
    @JsonTypeName("image")
    data class Image(val payload: MaxMediaToken) : MaxAttachmentRequest()
}

data class MaxKeyboardPayload(val buttons: List<List<MaxButton>>)
data class MaxMediaToken(val token: String)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MaxButton.Callback::class, name = "callback"),
    JsonSubTypes.Type(value = MaxButton.Link::class, name = "link"),
    JsonSubTypes.Type(value = MaxButton.RequestContact::class, name = "request_contact")
)
sealed class MaxButton {
    @JsonTypeName("callback")
    data class Callback(val text: String, val payload: String) : MaxButton()
    @JsonTypeName("link")
    data class Link(val text: String, val url: String) : MaxButton()
    @JsonTypeName("request_contact")
    data class RequestContact(val text: String) : MaxButton()
}

/** Result of POST /messages — the created message echoed back. */
data class SendMessageResult(val message: MaxMessage? = null)

/** Result of POST /uploads — the endpoint to upload bytes to. */
data class UploadEndpoint(val url: String)

/** Body for POST /answers — answer a callback query. Null fields are omitted by NON_NULL mapper. */
data class CallbackAnswer(val message: NewMessageBody? = null, val notification: String? = null)
