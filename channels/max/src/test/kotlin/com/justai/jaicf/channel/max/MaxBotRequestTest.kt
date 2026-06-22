package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.max.dto.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MaxBotRequestTest {

    private val sender = MaxUser(userId = 101, name = "Ivan")
    private val recipient = MaxRecipient(chatId = 202, chatType = "dialog", userId = 101)
    private fun msg(text: String?, att: List<MaxAttachment>? = null) =
        MaxMessage(sender = sender, recipient = recipient, body = MaxMessageBody(text = text, attachments = att))

    @Test fun `text request carries clientId, chatId, input`() {
        val r = MaxTextRequest(msg("привет"))
        assertEquals("101", r.clientId)
        assertEquals("привет", r.input)
        assertEquals(202L, r.chatId)
    }

    @Test fun `callback request uses payload as input and callback user as client`() {
        val cb = MaxCallback(callbackId = "cb1", payload = "press_yes", user = sender)
        val r = MaxCallbackRequest(cb, msg("choose"))
        assertEquals("101", r.clientId)
        assertEquals("press_yes", r.input)
        assertEquals(202L, r.chatId)
    }

    @Test fun `bot added request is an event`() {
        val r = MaxBotAddedRequest(BotAddedUpdate(chatId = 202, user = sender))
        assertEquals("101", r.clientId)
        assertEquals(MaxEvent.BOT_ADDED, r.input)
        assertEquals(202L, r.chatId)
    }
}
