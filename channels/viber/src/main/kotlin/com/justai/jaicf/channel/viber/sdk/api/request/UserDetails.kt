package com.justai.jaicf.channel.viber.sdk.api.request

import com.justai.jaicf.channel.viber.sdk.profile.UserProfile

data class UserDetailsRequest(
    val id: String
) : ApiRequest()

data class UserDetailsResponse(
    override val status: Int,
    override val statusMessage: String,
    val chatHostname: String,
    val user: UserProfile
) : ApiResponse(status, statusMessage)
