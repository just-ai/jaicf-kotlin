package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.channel.jaicp.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
abstract class Reply(val type: String) {
    abstract fun serialized(): String
}

@Serializable
data class TextReply(
    val text: String,
    val markup: String? = null,
    val tts: String? = null,
    val state: String? = null
) : Reply("text") {
    override fun serialized() = JSON.stringify(serializer(), this)
}

@Serializable
data class Button(
    val text: String,
    val transition: String? = null
)

@Serializable
data class ButtonsReply(
    val buttons: List<Button>,
    val state: String? = null
) : Reply("buttons") {
    constructor(button: Button) : this(arrayListOf(button))

    override fun serialized() = JSON.stringify(serializer(), this)
}

@Serializable
data class ImageReply(
    val imageUrl: String,
    val text: String? = null,
    val state: String? = null
) : Reply("image") {
    override fun serialized() = JSON.stringify(serializer(), this)
}

@Serializable
data class AudioReply(
    val audioUrl: String,
    val state: String? = null
) : Reply("audio") {

    override fun serialized() = JSON.stringify(serializer(), this)
}

@Serializable
class HangupReply(val state: String? = null) : Reply("hangup") {
    override fun serialized() = JSON.stringify(serializer(), this)
}

@Serializable
data class SwitchReply(
    val phoneNumber: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val firstMessage: String? = null,
    val lastMessage: String? = null,
    val closeChatPhrases: List<String> = emptyList(),
    val ignoreOffline: Boolean? = false,
    val destination: String? = null,
    val attributes: JsonObject? = null
) : Reply("switch") {
    override fun serialized() = JSON.stringify(serializer(), this)
}