package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.jaicp.asJaicpBotRequest
import com.justai.jaicf.channel.jaicp.logging.toEpochMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.time.OffsetDateTime

@Serializable
data class JaicpBotRequest(
    val data: JsonElement,
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
    val startProcessingTime: Long = OffsetDateTime.now().toEpochMillis()
) : BotRequest {
    override val type: BotRequestType = if (query != null) BotRequestType.QUERY else BotRequestType.EVENT
    override val clientId = channelUserId
    override val input: String = query ?: event ?: ""
}