package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.jaicp.deserialized
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Deferred

internal class ResponseSender(private val client: HttpClient, val legacy: Boolean) : WithLogger {
    suspend fun send(url: String, response: Deferred<JaicpBotResponse?>) {
        try {
            val botResponse = response.await() ?: return
            val text = when (legacy) {
                true -> JaicpPollingResponse(botResponse.questionId, botResponse).deserialized()
                false -> botResponse.deserialized()
            }
            logger.info(text)
            client.post<String>("$url/sendMessage".toUrl()) {
                body = TextContent(
                    text = text,
                    contentType = ContentType.Application.Json
                )
            }
        } catch (ex: Exception) {
            logger.debug("Failed to send message, exception: ", ex)
        }
    }
}