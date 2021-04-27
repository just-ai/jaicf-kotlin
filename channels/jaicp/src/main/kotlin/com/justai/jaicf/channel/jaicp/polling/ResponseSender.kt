package com.justai.jaicf.channel.jaicp.polling

import com.justai.jaicf.channel.http.isSuccess
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponseWithStatus
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.helpers.logging.WithLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred

internal class ResponseSender(private val client: HttpClient) : WithLogger {
    suspend fun send(url: String, deferredResponse: Deferred<JaicpBotResponseWithStatus>) {
        try {
            val responseWithStatus: JaicpBotResponseWithStatus = deferredResponse.await()

            if (responseWithStatus.response == null) {
                if (!responseWithStatus.statusCode.isSuccess()) {
                    logger.warn("Failed to process request, code ${responseWithStatus.statusCode}, message ${responseWithStatus.message}")
                }
                return
            }
            client.post<String>("$url/sendMessage".toUrl()) {
                body = responseWithStatus.response
                contentType(ContentType.Application.Json)
            }
        } catch (ex: Exception) {
            logger.debug("Failed to send message, exception: ", ex)
        }
    }
}
