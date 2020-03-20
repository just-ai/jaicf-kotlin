package com.justai.jaicf.channel.aimybox.api

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AimyboxBotRequest(
    val unit: String,
    val key: String? = null,
    val query: String,
    val data: JsonObject? = null
): BotRequest {
    override val type = BotRequestType.QUERY
    override val clientId = unit
    override val input = query
}

val BotRequest.aimybox
    get() = this as? AimyboxBotRequest