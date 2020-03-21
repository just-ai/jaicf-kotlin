package com.justai.jaicf.channel.aimybox.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface AimyboxReply

@Serializable
@SerialName("text")
data class TextReply(
    val text: String,
    val tts: String? = null,
    val lang: String? = null
): AimyboxReply

@Serializable
@SerialName("image")
data class ImageReply(
    @SerialName("imageUrl")
    val url: String
): AimyboxReply

@Serializable
@SerialName("audio")
data class AudioReply(
    val url: String
): AimyboxReply

@Serializable
@SerialName("buttons")
data class ButtonsReply(
    val buttons: List<Button> = listOf()
): AimyboxReply

interface Button {
    val text: String
}

@Serializable
@SerialName("text")
data class TextButton(
    override val text: String
): Button

@Serializable
@SerialName("url")
data class UrlButton(
    override val text: String,
    val url: String
): Button

@Serializable
@SerialName("payload")
data class PayloadButton(
    override val text: String,
    val payload: String
): Button