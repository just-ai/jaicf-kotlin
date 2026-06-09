package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.max.dto.*
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MaxBotRequestMappingTest {

    private val sender = MaxUser(userId = 101, name = "Ivan")
    private val recipient = MaxRecipient(chatId = 202, chatType = "dialog", userId = 101)
    private fun created(text: String?, att: List<MaxAttachment>? = null) =
        MessageCreatedUpdate(MaxMessage(sender, recipient, body = MaxMessageBody(text = text, attachments = att)))

    @Test fun `text message maps to MaxTextRequest`() {
        assertTrue(created("привет").toBotRequest() is MaxTextRequest)
    }

    @Test fun `contact attachment maps to MaxContactRequest even with caption`() {
        val u = created("подпись", listOf(MaxContactAttachment(MaxContactPayload(maxInfo = sender))))
        assertTrue(u.toBotRequest() is MaxContactRequest)
    }

    @Test fun `audio attachment maps to MaxAudioRequest even with caption`() {
        val u = created("подпись", listOf(MaxAudioAttachment(MaxMediaPayload(token = "t"))))
        assertTrue(u.toBotRequest() is MaxAudioRequest)
    }

    @Test fun `callback maps to MaxQueryRequest`() {
        val u = MessageCallbackUpdate(MaxCallback("cb1", "p", sender), created("x").message)
        assertTrue(u.toBotRequest() is MaxQueryRequest)
    }

    @Test fun `callback with null message returns null`() {
        val u = MessageCallbackUpdate(MaxCallback("cb1", "p", sender), null)
        assertNull(u.toBotRequest())
    }

    @Test fun `bot_added, bot_started, bot_removed map to their requests`() {
        assertTrue(BotAddedUpdate(202, sender).toBotRequest() is MaxBotAddedRequest)
        assertTrue(BotStartedUpdate(202, sender).toBotRequest() is MaxBotStartedRequest)
        assertTrue(BotRemovedUpdate(202, sender).toBotRequest() is MaxBotRemovedRequest)
    }

    @Test fun `empty message with no supported content returns null`() {
        assertNull(created(text = null, att = emptyList()).toBotRequest())
    }

    @Test fun `unknown update returns null`() {
        assertNull(UnknownMaxUpdate().toBotRequest())
    }
}
