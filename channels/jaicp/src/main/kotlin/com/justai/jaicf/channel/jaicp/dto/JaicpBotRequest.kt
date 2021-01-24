package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class JaicpBotRequest(
    val data: JsonElement?,
    val version: Int,
    val botId: String,
    val channelType: String,
    val channelBotId: String,
    val channelUserId: String,
    val questionId: String,
    val query: String? = null,
    val rawRequest: JsonObject,
    val userFrom: JsonElement,
    val event: String? = null,
    val startProcessingTime: Long = System.currentTimeMillis(),
    val timestamp: String = ""
) : BotRequest {
    override val type: BotRequestType = if (query != null) BotRequestType.QUERY else BotRequestType.EVENT
    override val clientId = channelUserId
    override val input: String = query ?: event ?: ""

    val raw: String get() = rawRequest.toString()

    fun stringify() = JSON.encodeToString(serializer(), this)

    internal fun isGatewayRequest() = rawRequest["commonType"]?.jsonPrimitive?.contentOrNull == "COMMON"

    internal fun asHttpBotRequest() = raw.asHttpBotRequest(stringify())
}