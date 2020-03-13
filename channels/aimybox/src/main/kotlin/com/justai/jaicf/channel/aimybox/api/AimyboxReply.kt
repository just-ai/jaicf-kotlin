package com.justai.jaicf.channel.aimybox.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface AimyboxReply {
    val type: String
}

@Serializable
data class TextReply(
    val text: String,
    val tts: String? = null,
    val lang: String? = null
): AimyboxReply {
    override val type = "text"
}

@Serializable
data class ImageReply(
    @SerialName("imageUrl")
    val url: String
): AimyboxReply {
    override val type = "image"
}

@Serializable
data class AudioReply(
    val url: String
): AimyboxReply {
    override val type = "audio"
}

@Serializable
data class ButtonsReply(
    val buttons: List<Button> = listOf()
): AimyboxReply {
    override val type = "buttons"
}

interface Button {
    val text: String
}

@Serializable
data class TextButton(
    override val text: String
): Button

@Serializable
data class UrlButton(
    override val text: String,
    val url: String
): Button

@Serializable
data class PayloadButton(
    override val text: String,
    val payload: String
): Button