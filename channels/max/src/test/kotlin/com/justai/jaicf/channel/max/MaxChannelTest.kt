package com.justai.jaicf.channel.max

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.invocationapi.InvocationEventRequest
import com.justai.jaicf.context.RequestContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MaxChannelTest {

    private val botApi = mockk<BotApi>(relaxed = true)
    private val channel = MaxChannel(botApi, maxBotToken = "t")

    private fun http(name: String) =
        HttpBotRequest(javaClass.getResource("/max/$name.json")!!.openStream())

    @Test fun `process dispatches a text update as MaxTextRequest and returns accepted`() {
        val captured = slot<BotRequest>()
        every { botApi.process(capture(captured), any(), any()) } returns Unit

        val response = channel.process(http("message_created_text"))

        verify { botApi.process(any(), any(), any()) }
        assertTrue(captured.captured is MaxTextRequest)
        assertEquals("101", (captured.captured as MaxTextRequest).clientId)
        assertEquals(202, response.statusCode) // accepted() == HttpStatusCode.ACCEPTED
    }

    @Test fun `process of unsupported update does not call botApi but still accepts`() {
        val response = channel.process(http("unknown_update"))
        verify(exactly = 0) { botApi.process(any(), any(), any()) }
        assertEquals(202, response.statusCode)
    }

    @Test fun `processInvocation dispatches a MaxBotRequest built from the template`() {
        val captured = slot<BotRequest>()
        every { botApi.process(capture(captured), any(), any()) } returns Unit

        channel.processInvocation(
            InvocationEventRequest(clientId = "101", input = "some_event", requestData = "{}"),
            RequestContext.DEFAULT
        )

        verify { botApi.process(any(), any(), any()) }
        assertEquals("101", (captured.captured as MaxBotRequest).clientId)
    }
}
