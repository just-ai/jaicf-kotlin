package com.justai.jaicf.channel.max.api

import com.justai.jaicf.channel.max.dto.MaxApiError
import com.justai.jaicf.channel.max.dto.NewMessageBody
import com.justai.jaicf.channel.max.dto.SendMessageResult
import com.justai.jaicf.channel.max.dto.maxObjectMapper
import com.justai.jaicf.channel.max.dto.toException
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

/**
 * HTTP client for the Max Bot API.
 *
 * @param token Bot access token; appended as `access_token` query parameter when non-null.
 * @param apiUrl Base URL of the Max Bot API (e.g. "https://botapi.max.ru").
 * @param engine Ktor [HttpClientEngine] — default is CIO; override with [MockEngine] in tests.
 */
class MaxBotApi(
    private val token: String?,
    private val apiUrl: String,
    engine: HttpClientEngine = CIO.create()
) {

    private val client = HttpClient(engine) {
        expectSuccess = false
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun sendMessage(chatId: Long, body: NewMessageBody): SendMessageResult =
        runBlocking { execute("$apiUrl/messages", chatId, body, SendMessageResult::class.java) }

    // -------------------------------------------------------------------------
    // Shared request/response helper
    // -------------------------------------------------------------------------

    private suspend fun <T : Any> execute(url: String, chatId: Long, requestBody: Any, responseType: Class<T>): T {
        val json = maxObjectMapper.writeValueAsString(requestBody)

        val response: HttpResponse = client.post(url) {
            if (token != null) parameter("access_token", token)
            parameter("chat_id", chatId)
            contentType(ContentType.Application.Json)
            body = json
        }

        val text = response.readText()
        val status = response.status.value

        if (response.status.isSuccess()) {
            return maxObjectMapper.readValue(text, responseType)
        }

        val error = runCatching {
            maxObjectMapper.readValue(text, MaxApiError::class.java)
        }.getOrNull()

        throw error.toException(status)
    }
}
