package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import kotlinx.serialization.json.Json

internal val JSON = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = false }

internal fun String.asJaicpBotRequest() = JSON.decodeFromString(JaicpBotRequest.serializer(), this)

internal fun String.asJaicpBotResponse() = JSON.decodeFromString(JaicpBotResponse.serializer(), this)

internal fun JaicpBotResponse.serialized() = JSON.encodeToString(JaicpBotResponse.serializer(), this)

internal fun String.toJson() = JSON.parseToJsonElement(this)