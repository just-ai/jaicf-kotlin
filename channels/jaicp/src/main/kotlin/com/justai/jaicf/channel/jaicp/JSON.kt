package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
import kotlinx.serialization.json.Json

internal val JSON = Json { ignoreUnknownKeys = true; isLenient = true }

internal fun String.asJaicpBotRequest() = JSON.decodeFromString(JaicpBotRequest.serializer(), this)

internal fun String.asJaicpBotResponse() = JSON.decodeFromString(JaicpBotResponse.serializer(), this)

internal fun JaicpBotResponse.deserialized() = JSON.encodeToString(JaicpBotResponse.serializer(), this)

internal fun JaicpPollingResponse.deserialized() = JSON.encodeToString(JaicpPollingResponse.serializer(), this)

internal fun String.toJson() = JSON.parseToJsonElement(this)