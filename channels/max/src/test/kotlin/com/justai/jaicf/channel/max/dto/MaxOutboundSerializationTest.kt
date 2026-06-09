package com.justai.jaicf.channel.max.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MaxOutboundSerializationTest {
    private fun json(v: Any) = maxObjectMapper.writeValueAsString(v)

    @Test fun `text body omits null fields`() {
        val s = json(NewMessageBody(text = "hi"))
        assertTrue("\"text\":\"hi\"" in s)
        assertFalse("null" in s)
    }

    @Test fun `inline keyboard with mixed buttons serializes by type`() {
        val body = NewMessageBody(
            text = "choose",
            attachments = listOf(
                MaxAttachmentRequest.InlineKeyboard(
                    MaxKeyboardPayload(listOf(listOf(
                        MaxButton.Callback(text = "Yes", payload = "yes"),
                        MaxButton.Link(text = "Site", url = "https://x"),
                        MaxButton.RequestContact(text = "Share")
                    )))
                )
            )
        )
        val s = json(body)
        assertTrue("\"type\":\"inline_keyboard\"" in s)
        assertTrue("\"type\":\"callback\"" in s && "\"payload\":\"yes\"" in s)
        assertTrue("\"type\":\"link\"" in s && "\"url\":\"https://x\"" in s)
        assertTrue("\"type\":\"request_contact\"" in s)
    }

    @Test fun `audio attachment carries token`() {
        val s = json(MaxAttachmentRequest.Audio(MaxMediaToken(token = "tok")))
        assertTrue("\"type\":\"audio\"" in s && "\"token\":\"tok\"" in s)
    }
}
