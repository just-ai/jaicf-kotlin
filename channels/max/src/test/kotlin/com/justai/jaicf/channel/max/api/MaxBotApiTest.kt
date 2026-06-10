package com.justai.jaicf.channel.max.api

import com.justai.jaicf.channel.max.dto.NewMessageBody
import com.justai.jaicf.channel.max.exception.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MaxBotApiTest {
    private fun api(handler: MockRequestHandler) =
        MaxBotApi(token = "t", apiUrl = "https://api.test", engine = MockEngine(handler), attachmentRetryDelayMs = 0)

    @Test fun `sendMessage posts to messages with access_token and chat_id`() {
        var seen: String? = null
        val api = api { req -> seen = req.url.toString()
            respond("""{"message":{"recipient":{"chat_id":202},"body":{"mid":"m","seq":1,"text":"hi"}}}""",
                HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        api.sendMessage(chatId = 202, NewMessageBody(text = "hi"))
        assertTrue(seen!!.contains("/messages"))
        assertTrue(seen!!.contains("access_token=t") && seen!!.contains("chat_id=202"))
    }

    @Test fun `401 throws MaxInvalidTokenException`() {
        val api = api { respond("""{"code":"verify.token","message":"bad"}""", HttpStatusCode.Unauthorized,
            headersOf(HttpHeaders.ContentType, "application/json")) }
        assertFailsWith<MaxInvalidTokenException> { api.sendMessage(1, NewMessageBody(text = "x")) }
    }
    @Test fun `403 throws MaxBotBlockedException`() {
        val api = api { respond("{}", HttpStatusCode.Forbidden) }
        assertFailsWith<MaxBotBlockedException> { api.sendMessage(1, NewMessageBody(text = "x")) }
    }
    @Test fun `429 throws MaxRateLimitException`() {
        val api = api { respond("{}", HttpStatusCode.TooManyRequests) }
        assertFailsWith<MaxRateLimitException> { api.sendMessage(1, NewMessageBody(text = "x")) }
    }

    @Test fun `sendMedia does uploads then binary then messages`() {
        val calls = mutableListOf<String>()
        val api = api { req -> calls += req.url.encodedPath
            when {
                req.url.encodedPath.endsWith("/uploads") -> respond("""{"url":"https://up.test/put"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json"))
                req.url.host == "up.test" -> respond("""{"token":"tok-1"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json"))
                req.url.encodedPath.endsWith("/messages") -> respond("""{"message":{"recipient":{"chat_id":1},"body":{"mid":"m","seq":1}}}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json"))
                else -> respond("{}", HttpStatusCode.OK)
            } }
        api.sendMedia(chatId = 1, type = "audio", bytes = byteArrayOf(1,2,3), text = null)
        assertTrue(calls.any { it.endsWith("/uploads") } && calls.any { it.endsWith("/messages") })
    }

    @Test fun `attachment_not_ready retries the messages call`() {
        var msgCalls = 0
        val api = api { req -> when {
            req.url.encodedPath.endsWith("/uploads") -> respond("""{"url":"https://up.test/put"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json"))
            req.url.host == "up.test" -> respond("""{"token":"tok"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json"))
            else -> { msgCalls++
                if (msgCalls == 1) respond("""{"code":"attachment.not.ready","message":"x"}""", HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType,"application/json"))
                else respond("""{"message":{"recipient":{"chat_id":1},"body":{"mid":"m","seq":1}}}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json")) } } }
        api.sendMedia(1, "audio", byteArrayOf(1), null)
        assertEquals(2, msgCalls)   // retried once
    }

    @Test fun `sendMedia with type file produces messages body with type file`() {
        var messagesBody: String? = null
        val api = api { req ->
            when {
                req.url.encodedPath.endsWith("/uploads") ->
                    respond("""{"url":"https://up.test/put"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                req.url.host == "up.test" ->
                    respond("""{"token":"tok-file"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                req.url.encodedPath.endsWith("/messages") -> {
                    messagesBody = req.body.toByteArray().decodeToString()
                    respond("""{"message":{"recipient":{"chat_id":1},"body":{"mid":"m","seq":1}}}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
                else -> respond("{}", HttpStatusCode.OK)
            }
        }
        api.sendMedia(chatId = 1, type = "file", bytes = byteArrayOf(1, 2, 3), text = null)
        assertTrue(messagesBody!!.contains(""""type":"file""""))
    }

    @Test fun `answerCallback posts to answers with callback_id`() {
        var url: String? = null
        val api = api { req -> url = req.url.toString(); respond("{}", HttpStatusCode.OK) }
        api.answerCallback(callbackId = "cb1", notification = "done")
        assertTrue(url!!.contains("/answers") && url!!.contains("callback_id=cb1"))
    }
}
