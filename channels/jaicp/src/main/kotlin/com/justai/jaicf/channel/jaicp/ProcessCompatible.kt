package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.fromRequest
import kotlinx.serialization.json.*

internal fun JaicpCompatibleBotChannel.processCompatible(
    botRequest: JaicpBotRequest
): JaicpBotResponse {
    val startTime = System.currentTimeMillis()
    val request = botRequest.raw.asHttpBotRequest(botRequest.stringify())
    val response = process(request)?.let { response ->
        val rawJson = JSON.encodeToJsonElement(response.output.toString())
        addRawReply(rawJson)
    } ?: throw RuntimeException("Failed to process compatible channel request")

    val processingTime = System.currentTimeMillis() - startTime
    return JaicpBotResponse.fromRequest(botRequest, response, processingTime)
}

private fun addRawReply(rawResponse: JsonElement) = buildJsonObject {
    "replies" to buildJsonArray {
        buildJsonObject {
            "type" to "raw"
            "body" to rawResponse
        }
    }
}
