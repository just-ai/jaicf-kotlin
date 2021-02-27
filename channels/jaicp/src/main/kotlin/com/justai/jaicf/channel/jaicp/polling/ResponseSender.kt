package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred

internal class ResponseSender(private val client: HttpClient) : WithLogger {
    suspend fun send(url: String, response: Deferred<JaicpBotResponse?>) {
        try {
            val serializedResponse: JaicpBotResponse = response.await() ?: return
            client.post<String>("$url/sendMessage".toUrl()) {
                body = serializedResponse
                contentType(ContentType.parse("application/json"))
            }
        } catch (ex: Exception) {
            logger.debug("Failed to send message, exception: ", ex)
        }
    }
}