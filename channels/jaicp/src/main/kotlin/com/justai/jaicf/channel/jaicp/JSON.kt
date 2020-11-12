package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

internal val JSON = Json(JsonConfiguration.Stable.copy(strictMode = false, encodeDefaults = false))

internal fun String.asJaicpBotRequest() = JSON.parse(JaicpBotRequest.serializer(), this)

internal fun String.asJaicpBotResponse() = JSON.parse(JaicpBotResponse.serializer(), this)

internal fun String.asJaicpPollingRequest() = JSON.parse(JaicpPollingRequest.serializer(), this)

internal fun JaicpBotResponse.deserialized() = JSON.stringify(JaicpBotResponse.serializer(), this)

internal fun JaicpPollingResponse.deserialized() = JSON.stringify(JaicpPollingResponse.serializer(), this)

internal fun String.toJson() = JSON.parseJson(this).jsonObject
