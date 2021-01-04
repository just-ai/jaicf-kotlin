package com.justai.jaicf.channel.jaicp.livechat

import kotlinx.serialization.Serializable

@Serializable
internal data class LiveChatEvent(
    private val commonType: String,
    val chatId: String,
    private val timestamp: Long,
    private val chatUserInfo: UserInfo
) {
    @Serializable
    data class UserInfo(
        private val id: String,
        private val firstName: String? = null,
        private val lastName: String? = null,
        private val userName: String? = null
    )
}

