package com.justai.jaicf.channel.jaicp.logging

import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import java.io.ByteArrayInputStream
import java.time.OffsetDateTime

fun String.asHttpBotRequest(jaicpBotRequest: JaicpBotRequest) = HttpBotRequest(
    stream = ByteArrayInputStream(this.toByteArray()),
    jaicpRawRequest = JSON.stringify(JaicpBotRequest.serializer(), jaicpBotRequest)
)

fun OffsetDateTime.toEpochMillis() = toEpochSecond() * 1000