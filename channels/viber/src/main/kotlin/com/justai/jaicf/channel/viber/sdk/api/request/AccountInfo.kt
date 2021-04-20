package com.justai.jaicf.channel.viber.sdk.api.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.justai.jaicf.channel.viber.sdk.message.Location
import com.justai.jaicf.channel.viber.sdk.profile.UserProfile

class AccountInfoRequest : ApiRequest()

data class AccountInfoResponse(
    override val status: Int,
    override val statusMessage: String,
    val id: String,
    val chatHostname: String,
    val name: String,
    val uri: String?,
    val icon: String?,
    val category: String?,
    val subcategory: String?,
    val location: Location?,
    val country: String?,
    val webhook: String?,
    val eventTypes: List<String>?,
    @JsonProperty("members")
    val subscribers: List<UserProfile>?,
    val subscribersCount: Int?
) : ApiResponse(status, statusMessage)
