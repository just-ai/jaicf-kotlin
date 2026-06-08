package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.max.dto.*
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MaxBotRequestFactoryTest {

    private val sender = MaxUser(userId = 101, name = "Ivan")
    private val recipient = MaxRecipient(chatId = 202, chatType = "dialog", userId = 101)
    private fun created(text: String?, att: List<MaxAttachment>? = null) =
        MessageCreatedUpdate(MaxMessage(sender, recipient, body = MaxMessageBody(text = text, attachments = att)))

    @Test fun `text message maps to MaxTextRequest`() {
        assertTrue(MaxBotRequestFactory.create(created("привет")) is MaxTextRequest)
    }

    @Test fun `contact attachment maps to MaxContactRequest even with caption`() {
        val u = created("подпись", listOf(MaxContactAttachment(MaxContactPayload(maxInfo = sender))))
        assertTrue(MaxBotRequestFactory.create(u) is MaxContactRequest)
    }

    @Test fun `audio attachment maps to MaxAudioRequest even with caption`() {
        val u = created("подпись", listOf(MaxAudioAttachment(MaxMediaPayload(token = "t"))))
        assertTrue(MaxBotRequestFactory.create(u) is MaxAudioRequest)
    }

    @Test fun `callback maps to MaxQueryRequest`() {
        val u = MessageCallbackUpdate(MaxCallback("cb1", "p", sender), created("x").message)
        assertTrue(MaxBotRequestFactory.create(u) is MaxQueryRequest)
    }

    @Test fun `callback with null message returns null`() {
        val u = MessageCallbackUpdate(MaxCallback("cb1", "p", sender), null)
        assertNull(MaxBotRequestFactory.create(u))
    }

    @Test fun `bot_added, bot_started, bot_removed map to their requests`() {
        assertTrue(MaxBotRequestFactory.create(BotAddedUpdate(202, sender)) is MaxBotAddedRequest)
        assertTrue(MaxBotRequestFactory.create(BotStartedUpdate(202, sender)) is MaxBotStartedRequest)
        assertTrue(MaxBotRequestFactory.create(BotRemovedUpdate(202, sender)) is MaxBotRemovedRequest)
    }

    @Test fun `empty message with no supported content returns null`() {
        assertNull(MaxBotRequestFactory.create(created(text = null, att = emptyList())))
    }

    @Test fun `unknown update returns null`() {
        assertNull(MaxBotRequestFactory.create(UnknownMaxUpdate()))
    }
}
