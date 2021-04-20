package com.justai.jaicf.channel.viber.sdk.api.request

data class ErrorResponse(
    override val status: Int,
    override val statusMessage: String?
) : ApiResponse(status, statusMessage)
