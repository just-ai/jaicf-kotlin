package com.justai.jaicf.core.test.channel

import com.justai.jaicf.channel.http.HttpBotRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.random.Random
import kotlin.test.assertEquals

class HttpBotRequestTest {
    @Test
    fun `should not throw exception on long request body`() {
        val randomBytes = Random.Default.nextBytes(65536)
        val request = HttpBotRequest(randomBytes.inputStream())
        val resultString = assertDoesNotThrow {
            request.receiveText(charset = Charsets.UTF_8)
        }
        assertEquals(
            randomBytes.decodeToString(),
            resultString
        )
    }
}