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
    engine: HttpClientEngine = CIO.create(),
    internal val attachmentRetryDelayMs: Long = 500L
) {

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
        runBlocking { execute("$apiUrl/messages", chatId, body, SendMessageResult::class.java) }

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
        val uploadEndpointResponse: HttpResponse = client.post("$apiUrl/uploads") {
            if (token != null) parameter("access_token", token)
            parameter("type", type)
        }
        val uploadEndpointText = uploadEndpointResponse.readText()
        if (!uploadEndpointResponse.status.isSuccess()) {
            val error = runCatching { maxObjectMapper.readValue(uploadEndpointText, MaxApiError::class.java) }.getOrNull()
            throw error.toException(uploadEndpointResponse.status.value)
        }
        val uploadEndpoint = maxObjectMapper.readValue(uploadEndpointText, UploadEndpoint::class.java)

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
        val uploadResponseText = uploadResponse.readText()
        if (!uploadResponse.status.isSuccess()) {
            val error = runCatching { maxObjectMapper.readValue(uploadResponseText, MaxApiError::class.java) }.getOrNull()
            throw error.toException(uploadResponse.status.value)
        }
        val mediaToken = maxObjectMapper.readValue(uploadResponseText, MaxMediaToken::class.java)

        // Step 3: send message with attachment token, retry on attachment.not.ready
        val attachment = attachmentFor(type, mediaToken.token)
        val messageBody = NewMessageBody(text = text, attachments = listOf(attachment))

        var lastException: MaxApiException? = null
        repeat(MAX_ATTACHMENT_RETRIES) { attempt ->
            try {
                return execute("$apiUrl/messages", chatId, messageBody, SendMessageResult::class.java)
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
            val text = bytes.toString(Charsets.UTF_8)
            val error = runCatching { maxObjectMapper.readValue(text, MaxApiError::class.java) }.getOrNull()
            throw error.toException(response.status.value)
        }
        return bytes
    }

    private fun attachmentFor(type: String, token: String): MaxAttachmentRequest = when (type) {
        "image" -> MaxAttachmentRequest.Image(MaxMediaToken(token))
        else    -> MaxAttachmentRequest.Audio(MaxMediaToken(token))
    }

    private suspend fun answerCallbackSuspend(callbackId: String, message: NewMessageBody?, notification: String?) {
        val json = maxObjectMapper.writeValueAsString(CallbackAnswer(message, notification))
        val response: HttpResponse = client.post("$apiUrl/answers") {
            if (token != null) parameter("access_token", token)
            parameter("callback_id", callbackId)
            contentType(ContentType.Application.Json)
            body = json
        }
        if (!response.status.isSuccess()) {
            val text = response.readText()
            val error = runCatching { maxObjectMapper.readValue(text, MaxApiError::class.java) }.getOrNull()
            throw error.toException(response.status.value)
        }
    }

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
