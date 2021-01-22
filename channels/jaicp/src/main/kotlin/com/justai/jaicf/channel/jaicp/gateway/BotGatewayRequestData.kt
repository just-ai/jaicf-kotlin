package com.justai.jaicf.channel.jaicp.gateway

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class BotGatewayRequestData(
    val commonType: String,
    val chatId: String,
    val timestamp: Long,
    val chatUserInfo: UserInfo? = null
) {
    @Serializable
    data class UserInfo(
        val id: String,
        val firstName: String? = null,
        val lastName: String? = null,
        val userName: String? = null,
        val requestData: JsonObject? = null
    )
}
