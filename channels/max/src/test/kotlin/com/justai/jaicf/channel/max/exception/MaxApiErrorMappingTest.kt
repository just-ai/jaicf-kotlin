package com.justai.jaicf.channel.max.exception

import com.justai.jaicf.channel.max.dto.MaxApiError
import com.justai.jaicf.channel.max.dto.toException
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MaxApiErrorMappingTest {
    private fun err(code: String) = MaxApiError(code = code, message = "m")

    @Test fun `401 maps to invalid token`() {
        assertTrue(err("verify.token").toException(401) is MaxInvalidTokenException)
    }
    @Test fun `429 maps to rate limit`() {
        assertTrue(err("too.many.requests").toException(429) is MaxRateLimitException)
    }
    @Test fun `403 maps to bot blocked`() {
        assertTrue(err("access.denied").toException(403) is MaxBotBlockedException)
    }
    @Test fun `other status maps to base MaxApiException`() {
        val e = err("unknown").toException(500)
        assertTrue(e is MaxApiException && e !is MaxRateLimitException)
    }
    @Test fun `null error still maps by status`() {
        assertTrue((null as MaxApiError?).toException(429) is MaxRateLimitException)
    }
}
