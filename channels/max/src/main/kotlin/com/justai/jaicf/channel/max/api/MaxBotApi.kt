package com.justai.jaicf.channel.max.api

import com.justai.jaicf.channel.max.dto.CallbackAnswer
import com.justai.jaicf.channel.max.dto.MaxApiError
import com.justai.jaicf.channel.max.dto.MaxAttachmentRequest
import com.justai.jaicf.channel.max.dto.MaxMediaToken
import com.justai.jaicf.channel.max.dto.NewMessageBody
import com.justai.jaicf.channel.max.dto.SendMessageResult
import com.justai.jaicf.channel.max.dto.UploadEndpoint
import com.justai.jaicf.channel.max.dto.maxObjectMapper
import com.justai.jaicf.channel.max.dto.toException
import com.justai.jaicf.channel.max.exception.MaxApiException
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * HTTP client for the Max Bot API.
 *
 * @param token Bot access token; appended as `access_token` query parameter when non-null.
 * @param apiUrl Base URL of the Max Bot API (e.g. "https://botapi.max.ru").
 * @param engine Ktor [HttpClientEngine] — default is CIO; override with [MockEngine] in tests.
 * @param attachmentRetryDelayMs Delay between `attachment.not.ready` retries (ms). Exposed for testing.
 */
class MaxBotApi(
    private val token: String?,
    private val apiUrl: String,
    engine: HttpClientEngine = CIO.create { requestTimeout = 30_000 },
    internal val attachmentRetryDelayMs: Long = 500L
) {

    private val baseUrl = apiUrl.trimEnd('/')

    private val client = HttpClient(engine) {
        expectSuccess = false
    }

    companion object {
        private const val MAX_ATTACHMENT_RETRIES = 3
    }

    fun sendMessage(chatId: Long, body: NewMessageBody): SendMessageResult =
        runBlocking { execute("$baseUrl/messages", chatId, body, SendMessageResult::class.java) }

    /** Uploads [bytes] of the given [type] (audio/image/file) and sends them to [chatId] (see [sendMediaSuspend]). */
    fun sendMedia(chatId: Long, type: String, bytes: ByteArray, text: String? = null): SendMessageResult =
        runBlocking { sendMediaSuspend(chatId, type, bytes, text) }

    /** URL-based overload: fetches the bytes from [url] then delegates to the bytes overload. */
    fun sendMedia(chatId: Long, type: String, url: String, text: String? = null): SendMessageResult =
        runBlocking { sendMediaSuspend(chatId, type, fetchBytes(url), text) }

    /** Answers a callback query: `POST /answers?callback_id=…` with an optional [message] and [notification]. */
    fun answerCallback(callbackId: String, message: NewMessageBody? = null, notification: String? = null): Unit =
        runBlocking { answerCallbackSuspend(callbackId, message, notification) }

    /**
     * 3-step media send: obtain an upload endpoint, upload the bytes, then post a message
     * referencing the resulting attachment token (retrying while the attachment is still processing).
     */
    private suspend fun sendMediaSuspend(chatId: Long, type: String, bytes: ByteArray, text: String?): SendMessageResult {
        val endpoint = requestUploadEndpoint(type)
        val mediaToken = uploadBytes(endpoint.url, bytes)
        val body = NewMessageBody(text = text, attachments = listOf(attachmentFor(type, mediaToken)))
        return sendWithAttachmentRetry(chatId, body)
    }

    /** Step 1 — `POST /uploads?type=…` returning the endpoint to upload the binary to. */
    private suspend fun requestUploadEndpoint(type: String): UploadEndpoint {
        val response = client.post<HttpResponse>("$baseUrl/uploads") {
            accessToken()
            parameter("type", type)
        }
        return maxObjectMapper.readValue(checkOrThrow(response), UploadEndpoint::class.java)
    }

    /** Step 2 — upload [bytes] to [uploadUrl] as multipart form-data, returning the attachment token. */
    private suspend fun uploadBytes(uploadUrl: String, bytes: ByteArray): String {
        val response = client.post<HttpResponse>(uploadUrl) { body = multipartOf(bytes) }
        return maxObjectMapper.readValue(checkOrThrow(response), MaxMediaToken::class.java).token
    }

    /** Step 3 — `POST /messages`, retrying while Max reports the attachment is not yet processed. */
    private suspend fun sendWithAttachmentRetry(chatId: Long, body: NewMessageBody): SendMessageResult {
        repeat(MAX_ATTACHMENT_RETRIES - 1) {
            try {
                return execute("$baseUrl/messages", chatId, body, SendMessageResult::class.java)
            } catch (e: MaxApiException) {
                if (!e.isAttachmentNotReady) throw e
                delay(attachmentRetryDelayMs)
            }
        }
        // Final attempt: let any exception propagate to the caller.
        return execute("$baseUrl/messages", chatId, body, SendMessageResult::class.java)
    }

    private suspend fun answerCallbackSuspend(callbackId: String, message: NewMessageBody?, notification: String?) {
        val response = client.post<HttpResponse>("$baseUrl/answers") {
            accessToken()
            parameter("callback_id", callbackId)
            jsonBody(CallbackAnswer(message, notification))
        }
        checkOrThrow(response)
    }

    private suspend fun fetchBytes(url: String): ByteArray {
        val response = client.get<HttpResponse>(url)
        val bytes = response.readBytes()
        if (!response.status.isSuccess()) raiseError(response.status, bytes.toString(Charsets.UTF_8))
        return bytes
    }

    private suspend fun <T : Any> execute(url: String, chatId: Long, requestBody: Any, responseType: Class<T>): T {
        val response = client.post<HttpResponse>(url) {
            accessToken()
            parameter("chat_id", chatId)
            jsonBody(requestBody)
        }
        return maxObjectMapper.readValue(checkOrThrow(response), responseType)
    }

    /** Reads the response body, raising the matching typed [MaxApiException] on a non-2xx status. */
    private suspend fun checkOrThrow(response: HttpResponse): String {
        val text = response.readText()
        if (!response.status.isSuccess()) raiseError(response.status, text)
        return text
    }

    private fun raiseError(status: HttpStatusCode, body: String): Nothing {
        val error = runCatching { maxObjectMapper.readValue(body, MaxApiError::class.java) }.getOrNull()
        throw error.toException(status.value)
    }

    private fun HttpRequestBuilder.accessToken() = token?.let { parameter("access_token", it) }

    private fun HttpRequestBuilder.jsonBody(value: Any) {
        contentType(ContentType.Application.Json)
        body = maxObjectMapper.writeValueAsString(value)
    }

    private fun multipartOf(bytes: ByteArray) = MultiPartFormDataContent(
        formData {
            append("data", bytes, Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=upload")
            })
        }
    )

    private fun attachmentFor(type: String, token: String): MaxAttachmentRequest = when (type) {
        "image" -> MaxAttachmentRequest.Image(MaxMediaToken(token))
        "file" -> MaxAttachmentRequest.File(MaxMediaToken(token))
        else -> MaxAttachmentRequest.Audio(MaxMediaToken(token))
    }

    private val MaxApiException.isAttachmentNotReady: Boolean
        get() = httpStatus == 400 && code == "attachment.not.ready"
}
