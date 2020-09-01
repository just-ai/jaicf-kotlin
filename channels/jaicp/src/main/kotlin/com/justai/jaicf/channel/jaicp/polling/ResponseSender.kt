package com.justai.jaicf.channel.jaicp.polling


import com.justai.jaicf.channel.jaicp.deserialized
import com.justai.jaicf.channel.jaicp.dto.JaicpPollingResponse
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType

internal class ResponseSender(private val client: HttpClient) : WithLogger {
    suspend fun processResponse(url: String, result: JaicpPollingResponse) {
        try {
            val text = result.deserialized()
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