package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
@Polymorphic
internal sealed class Reply(val type: String)

@Serializable
internal data class TextReply(
    val text: String,
    val markup: String? = null,
    val tts: String? = null,
    val state: String? = null
) : Reply("text")

@Serializable
internal data class Button(
    val text: String,
    val transition: String? = null
)

@Serializable
internal data class ButtonsReply(
    val buttons: List<Button>,
    val state: String? = null
) : Reply("buttons") {
    constructor(button: Button) : this(arrayListOf(button))
}

@Serializable
internal data class ImageReply(
    val imageUrl: String,
    val text: String? = null,
    val state: String? = null
) : Reply("image")

@Serializable
internal data class AudioReply(
    val audioUrl: String,
    val state: String? = null
) : Reply("audio")

@Serializable
internal class HangupReply(val state: String? = null) : Reply("hangup")

@Serializable
internal data class SwitchReply(
    val phoneNumber: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val firstMessage: String? = null,
    val lastMessage: String? = null,
    val closeChatPhrases: List<String> = emptyList(),
    val ignoreOffline: Boolean? = false,
    val destination: String? = null,
    val attributes: JsonObject? = null
) : Reply("switch")