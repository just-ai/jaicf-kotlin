package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpAsyncResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpErrorResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpResponse
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred

internal class ResponseSender(private val client: HttpClient) : WithLogger {
    suspend fun send(url: String, deferredResponse: Deferred<JaicpResponse>) {
        try {
            val response = deferredResponse.await()
            when (response) {
                is JaicpBotResponse -> send(url, response)
                is JaicpErrorResponse -> logger.warn("Failed to process request, message ${response.message}")
                is JaicpAsyncResponse -> logger.trace("Ignored an async response")
            }
        } catch (ex: Exception) {
            logger.debug("Failed to send message, exception: ", ex)
        }
    }

    private suspend fun send(url: String, response: JaicpBotResponse) {
        client.post<String>("$url/sendMessage".toUrl()) {
            body = response
            contentType(ContentType.Application.Json)
        }
    }
}
