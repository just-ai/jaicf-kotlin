package com.justai.jaicf.channel.jaicp.logging

import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import java.io.ByteArrayInputStream

internal fun String.asHttpBotRequest(jaicpBotRequest: JaicpBotRequest) = HttpBotRequest(
    stream = ByteArrayInputStream(this.toByteArray()),
    requestMetadata = JSON.stringify(JaicpBotRequest.serializer(), jaicpBotRequest)
)

