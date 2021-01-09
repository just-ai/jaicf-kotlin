package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class LiveChatEvent(
    val commonType: String,
    val chatId: String,
    val timestamp: Long,
    val chatUserInfo: UserInfo,
    val originalRequest: JsonObject
) {
    @Serializable
    data class UserInfo(
        val id: String,
        val firstName: String? = null,
        val lastName: String? = null,
        val userName: String? = null
    )

    fun getHttpRequest(r: JaicpBotRequest) = originalRequest.toString().asHttpBotRequest(r.stringify())
}

