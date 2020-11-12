package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Serializable

@Serializable
data class JaicpPingRequest(
    val requestType: String,
    val botId: String
)
