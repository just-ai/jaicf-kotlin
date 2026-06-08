package com.justai.jaicf.channel.max.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
    property = "type", defaultImpl = UnknownMaxAttachment::class)
@JsonSubTypes(
    JsonSubTypes.Type(value = MaxContactAttachment::class, name = "contact"),
    JsonSubTypes.Type(value = MaxAudioAttachment::class, name = "audio")
)
sealed class MaxAttachment

data class MaxContactPayload(val vcfInfo: String? = null, val maxInfo: MaxUser? = null)
data class MaxContactAttachment(val payload: MaxContactPayload) : MaxAttachment()

data class MaxMediaPayload(val url: String? = null, val token: String? = null)
data class MaxAudioAttachment(val payload: MaxMediaPayload) : MaxAttachment()

class UnknownMaxAttachment : MaxAttachment()
