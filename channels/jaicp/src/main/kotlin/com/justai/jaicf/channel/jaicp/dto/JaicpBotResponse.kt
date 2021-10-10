package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.api.BotResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class JaicpResponse

@Serializable
object JaicpAsyncResponse : JaicpResponse()

@Serializable
class JaicpErrorResponse(val message: String) : JaicpResponse()

@Serializable
data class JaicpBotResponse(
    val data: JsonElement,
    val botId: String,
    val accountId: String,
    val channelType: String,
    val channelBotId: String,
    val channelUserId: String,
    val questionId: String,
    val query: String?,
    var timestamp: Long,
    val currentState: String,
    var processingTime: Long,
    val requestType: String = "query",
    val version: Int = 1,
    val requestData: JsonObject,
) : BotResponse, JaicpResponse()

fun JaicpBotResponse.Companion.fromRequest(
    jaicpBotRequest: JaicpBotRequest,
    rawResponse: JsonElement,
    processingTime: Long = 0,
    currentState: String = "/",
) = JaicpBotResponse(
    data = rawResponse,
    botId = jaicpBotRequest.botId,
    accountId = jaicpBotRequest.botId,
    channelType = jaicpBotRequest.channelType,
    channelBotId = jaicpBotRequest.channelBotId,
    channelUserId = jaicpBotRequest.channelUserId,
    questionId = jaicpBotRequest.questionId,
    query = jaicpBotRequest.query,
    timestamp = System.currentTimeMillis(),
    currentState = currentState,
    processingTime = processingTime,
    requestData = jaicpBotRequest.rawRequest
)
