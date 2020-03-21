package com.justai.jaicf.channel.aimybox.api

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.channel.aimybox.AimyboxEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AimyboxBotRequest(
    val unit: String,
    val key: String? = null,
    val query: String,
    val data: JsonObject? = null
): BotRequest {
    override val clientId = unit

    override val type = when {
        query.isNotEmpty() -> BotRequestType.QUERY
        else -> BotRequestType.EVENT
    }

    override val input = when {
        query.isNotEmpty() -> query
        else -> AimyboxEvent.START
    }
}

val BotRequest.aimybox
    get() = this as? AimyboxBotRequest