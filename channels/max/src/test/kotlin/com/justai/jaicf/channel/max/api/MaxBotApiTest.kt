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
        MaxBotApi(token = "t", apiUrl = "https://api.test", engine = MockEngine(handler))

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
}
