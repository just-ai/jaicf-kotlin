package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Serializable

@Serializable
internal sealed class Reply(val type: String)

@Serializable
internal data class TextReply(
    val text: String,
    val markup: String? = null,
    val tts: String? = null
) : Reply("text")

@Serializable
internal data class Button(
    val text: String,
    val transition: String? = null
)

@Serializable
internal data class ButtonsReply(val buttons: List<Button>) : Reply("button") {
    constructor(button: Button) : this(arrayListOf(button))
}

@Serializable
internal data class ImageReply(val imageUrl: String, val text: String? = null) : Reply("image")

@Serializable
internal data class AudioReply(val audioUrl: String) : Reply("audio")

@Serializable
internal class HangupReply : Reply("hangup")