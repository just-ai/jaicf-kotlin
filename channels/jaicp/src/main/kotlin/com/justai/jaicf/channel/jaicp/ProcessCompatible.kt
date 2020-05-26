package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.create
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray
import java.lang.RuntimeException
import java.time.OffsetDateTime

internal fun JaicpCompatibleBotChannel.processCompatible(
    botRequest: JaicpBotRequest
): JaicpBotResponse {
    val startTime = OffsetDateTime.now().toEpochSecond()
    val request = botRequest.rawRequest.toString().asHttpBotRequest()
    val response = process(request)?.let { response ->
        val rawJson = JSON.parseJson(response.output.toString())
        addRawReply(rawJson)
    } ?: throw RuntimeException("Failed to process compatible channel request")

    val processingTime = OffsetDateTime.now().toEpochSecond() - startTime
    return JaicpBotResponse.create(botRequest, response, processingTime)
}

private fun addRawReply(rawResponse: JsonElement) = json {
    "replies" to jsonArray {
        +(json {
            "type" to "raw"
            "body" to rawResponse
        })
    }
}
