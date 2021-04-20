package com.justai.jaicf.channel.viber.sdk.api.request

data class WebhookRequest(
    val url: String,
    val eventTypes: List<String>
) : ApiRequest()

data class WebhookResponse(
    override val status: Int,
    override val statusMessage: String,
    val chatHostname: String,
    val eventTypes: List<String>
) : ApiResponse(status, statusMessage)
