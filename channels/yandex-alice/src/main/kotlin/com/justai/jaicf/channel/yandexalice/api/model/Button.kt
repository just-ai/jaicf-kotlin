package com.justai.jaicf.channel.yandexalice.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Button(
    val title: String,
    val payload: JsonObject? = null,
    val url: String? = null,
    val hide: Boolean = false
)