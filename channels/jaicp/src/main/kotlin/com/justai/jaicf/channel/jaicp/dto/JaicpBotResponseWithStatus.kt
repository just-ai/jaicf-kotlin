package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.channel.http.HttpStatusCode
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.http.asTextHttpBotResponse
import com.justai.jaicf.channel.jaicp.serialized

data class JaicpBotResponseWithStatus(
    val response: JaicpBotResponse?,
    val statusCode: HttpStatusCode = HttpStatusCode.OK,
    val message: String = ""
)

fun JaicpBotResponse.withStatus(statusCode: HttpStatusCode = HttpStatusCode.OK, message: String = "") =
    JaicpBotResponseWithStatus(this, statusCode = statusCode, message = message)

fun JaicpBotResponseWithStatus.asHttpBotResponse() =
    response?.serialized()?.asJsonHttpBotResponse(statusCode) ?: message.asTextHttpBotResponse(statusCode)
