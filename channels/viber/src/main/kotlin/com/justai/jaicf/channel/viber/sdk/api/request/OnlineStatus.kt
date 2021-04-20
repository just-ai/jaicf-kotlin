package com.justai.jaicf.channel.viber.sdk.api.request

data class OnlineStatusRequest(
    val ids: List<String>
) : ApiRequest()

data class OnlineStatusResponse(
    override val status: Int,
    override val statusMessage: String,
    val users: List<UserStatus>
) : ApiResponse(status, statusMessage)

data class UserStatus(
    val id: String,
    val onlineStatus: Int,
    val onlineStatusMessage: String,
    val lastOnline: String?
)
