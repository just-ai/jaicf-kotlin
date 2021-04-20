package com.justai.jaicf.channel.viber.sdk.api.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.justai.jaicf.channel.viber.sdk.message.Message
import com.justai.jaicf.channel.viber.sdk.profile.BotProfile

data class SendMessageRequest(
    @JsonUnwrapped
    val message: Message,
    @JsonProperty("receiver")
    val receiverId: String,
    val sender: BotProfile
) : ApiRequest()

data class SendMessageResponse(
    override val status: Int,
    override val statusMessage: String,
    val messageToken: String,
    val chatHostname: String
) : ApiResponse(status, statusMessage)
