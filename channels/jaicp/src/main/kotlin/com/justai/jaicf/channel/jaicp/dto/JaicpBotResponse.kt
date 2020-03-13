package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.api.BotResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.OffsetDateTime

@Serializable
data class JaicpBotResponse(
    val data: JsonElement,
    val botId: String,
    val accountId: String,
    val channelType: String,
    val channelBotId: String,
    val channelUserId: String,
    val questionId: String,
    val query: String,
    val timestamp: Long,
    val currentState: String,
    var processingTime: Long,
    val requestType: String = "query",
    val version: Int = 1
): BotResponse

fun JaicpBotResponse.Companion.create(jaicpBotRequest: JaicpBotRequest, rawResponse: JsonElement) =
    JaicpBotResponse(
        data = rawResponse,
        botId = jaicpBotRequest.botId,
        accountId = jaicpBotRequest.botId,
        channelType = jaicpBotRequest.channelType,
        channelBotId = jaicpBotRequest.channelBotId,
        channelUserId = jaicpBotRequest.channelUserId,
        questionId = jaicpBotRequest.questionId,
        query = jaicpBotRequest.query!!,
        timestamp = OffsetDateTime.now().toEpochSecond(),
        currentState = "todo",
        processingTime = 0L
    )