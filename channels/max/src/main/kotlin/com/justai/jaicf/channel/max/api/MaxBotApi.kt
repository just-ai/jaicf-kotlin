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

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    fun sendMessage(chatId: Long, body: NewMessageBody): SendMessageResult =
        runBlocking { execute("$baseUrl/messages", chatId, body, SendMessageResult::class.java) }

    /**
     * 3-step media upload:
     * 1. POST /uploads?type=… → [UploadEndpoint]
     * 2. POST bytes to upload URL → media token
     * 3. POST /messages with attachment token (retries on `attachment.not.ready`)
     */
    fun sendMedia(chatId: Long, type: String, bytes: ByteArray, text: String? = null): SendMessageResult =
        runBlocking { sendMediaSuspend(chatId, type, bytes, text) }

    /**
     * URL-based overload: fetches the bytes from [url] then delegates to the bytes overload.
     */
    fun sendMedia(chatId: Long, type: String, url: String, text: String? = null): SendMessageResult =
        runBlocking {
            val bytes = fetchBytes(url)
            sendMediaSuspend(chatId, type, bytes, text)
        }

    /**
     * Answers a callback query: POST /answers?access_token=…&callback_id=[callbackId]
     *
     * @param callbackId The callback query ID to answer.
     * @param message    Optional message to send back to the user.
     * @param notification Optional notification text shown as a toast.
     */
    fun answerCallback(callbackId: String, message: NewMessageBody? = null, notification: String? = null): Unit =
        runBlocking { answerCallbackSuspend(callbackId, message, notification) }

    // -------------------------------------------------------------------------
    // Suspend implementations
    // -------------------------------------------------------------------------

    private suspend fun sendMediaSuspend(chatId: Long, type: String, bytes: ByteArray, text: String?): SendMessageResult {
        // Step 1: obtain upload endpoint
        val uploadEndpointResponse: HttpResponse = client.post("$baseUrl/uploads") {
            if (token != null) parameter("access_token", token)
            parameter("type", type)
        }
        val uploadEndpoint = maxObjectMapper.readValue(
            checkOrThrow(uploadEndpointResponse), UploadEndpoint::class.java
        )

        // Step 2: upload binary to the returned URL
        val uploadResponse: HttpResponse = client.post(uploadEndpoint.url) {
            body = MultiPartFormDataContent(
                formData {
                    append("data", bytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=upload")
                    })
                }
            )
        }
        val mediaToken = maxObjectMapper.readValue(
            checkOrThrow(uploadResponse), MaxMediaToken::class.java
        )

        // Step 3: send message with attachment token, retry on attachment.not.ready
        val attachment = attachmentFor(type, mediaToken.token)
        val messageBody = NewMessageBody(text = text, attachments = listOf(attachment))

        var lastException: MaxApiException? = null
        repeat(MAX_ATTACHMENT_RETRIES) { attempt ->
            try {
                return execute("$baseUrl/messages", chatId, messageBody, SendMessageResult::class.java)
            } catch (e: MaxApiException) {
                if (e.httpStatus == 400 && e.code == "attachment.not.ready") {
                    lastException = e
                    if (attempt < MAX_ATTACHMENT_RETRIES - 1) {
                        delay(attachmentRetryDelayMs)
                    }
                } else {
                    throw e
                }
            }
        }
        throw lastException!!
    }

    private suspend fun fetchBytes(url: String): ByteArray {
        val response: HttpResponse = client.get(url)
        val bytes = response.readBytes()
        if (!response.status.isSuccess()) {
            val bodyText = bytes.toString(Charsets.UTF_8)
            val error = runCatching { maxObjectMapper.readValue(bodyText, MaxApiError::class.java) }.getOrNull()
            throw error.toException(response.status.value)
        }
        return bytes
    }

    private fun attachmentFor(type: String, token: String): MaxAttachmentRequest = when (type) {
        "image" -> MaxAttachmentRequest.Image(MaxMediaToken(token))
        "file"  -> MaxAttachmentRequest.File(MaxMediaToken(token))
        else    -> MaxAttachmentRequest.Audio(MaxMediaToken(token))
    }

    private suspend fun answerCallbackSuspend(callbackId: String, message: NewMessageBody?, notification: String?) {
        val json = maxObjectMapper.writeValueAsString(CallbackAnswer(message, notification))
        val response: HttpResponse = client.post("$baseUrl/answers") {
            if (token != null) parameter("access_token", token)
            parameter("callback_id", callbackId)
            contentType(ContentType.Application.Json)
            body = json
        }
        checkOrThrow(response)
    }

    // -------------------------------------------------------------------------
    // Shared request/response helpers
    // -------------------------------------------------------------------------

    private suspend fun checkOrThrow(response: HttpResponse): String {
        val text = response.readText()
        if (!response.status.isSuccess()) {
            val error = runCatching { maxObjectMapper.readValue(text, MaxApiError::class.java) }.getOrNull()
            throw error.toException(response.status.value)
        }
        return text
    }

    private suspend fun <T : Any> execute(url: String, chatId: Long, requestBody: Any, responseType: Class<T>): T {
        val json = maxObjectMapper.writeValueAsString(requestBody)

        val response: HttpResponse = client.post(url) {
            if (token != null) parameter("access_token", token)
            parameter("chat_id", chatId)
            contentType(ContentType.Application.Json)
            body = json
        }

        return maxObjectMapper.readValue(checkOrThrow(response), responseType)
    }
}
