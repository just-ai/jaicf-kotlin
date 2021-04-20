package com.justai.jaicf.channel.viber.sdk.api.request

open class ApiRequest

open class ApiResponse(
    open val status: Int,
    open val statusMessage: String?
)
