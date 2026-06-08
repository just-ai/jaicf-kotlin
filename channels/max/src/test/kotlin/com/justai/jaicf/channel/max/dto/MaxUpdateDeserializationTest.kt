package com.justai.jaicf.channel.max.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MaxUpdateDeserializationTest {

    private fun read(name: String): String =
        javaClass.getResource("/max/$name.json")!!.readText()

    private inline fun <reified T : MaxUpdate> parse(name: String): T {
        val raw = MaxObjectMapper.mapper.readValue(read(name), MaxUpdate::class.java)
        assertTrue(raw is T, "Expected ${T::class.simpleName} but got ${raw::class.simpleName}")
        return raw
    }

    @Test fun `message_created with text`() {
        val u = parse<MessageCreatedUpdate>("message_created_text")
        assertEquals(101L, u.message.sender!!.userId)
        assertEquals(202L, u.message.recipient.chatId)
        assertEquals("dialog", u.message.recipient.chatType)
        assertEquals("привет", u.message.body.text)
        assertTrue(u.message.body.attachments.isNullOrEmpty())
        assertEquals(1700000000000L, u.timestamp)
    }

    @Test fun `message_created with contact attachment`() {
        val u = parse<MessageCreatedUpdate>("message_created_contact")
        val contact = u.message.body.attachments!!.filterIsInstance<MaxContactAttachment>().single()
        assertEquals(101L, contact.payload.maxInfo!!.userId)
    }

    @Test fun `message_created with audio attachment`() {
        val u = parse<MessageCreatedUpdate>("message_created_audio")
        val audio = u.message.body.attachments!!.filterIsInstance<MaxAudioAttachment>().single()
        assertEquals("tok-1", audio.payload.token)
    }

    @Test fun `message_callback`() {
        val u = parse<MessageCallbackUpdate>("message_callback")
        assertEquals("press_yes", u.callback.payload)
        assertEquals(101L, u.callback.user.userId)
        assertEquals("cb1", u.callback.callbackId)
        assertEquals(202L, u.message!!.recipient.chatId)
    }

    @Test fun `bot_added`() {
        val u = parse<BotAddedUpdate>("bot_added")
        assertEquals(202L, u.chatId)
        assertEquals(101L, u.user.userId)
        assertEquals(false, u.isChannel)
    }

    @Test fun `bot_started`() {
        val u = parse<BotStartedUpdate>("bot_started")
        assertEquals(202L, u.chatId)
        assertEquals(101L, u.user.userId)
        assertEquals("start_payload", u.payload)
    }

    @Test fun `bot_removed`() {
        val u = parse<BotRemovedUpdate>("bot_removed")
        assertEquals(202L, u.chatId)
        assertEquals(false, u.isChannel)
    }

    @Test fun `unknown update_type deserializes to UnknownMaxUpdate`() {
        val u = MaxObjectMapper.mapper.readValue(read("unknown_update"), MaxUpdate::class.java)
        assertTrue(u is UnknownMaxUpdate)
    }

    @Test fun `unknown attachment type deserializes to UnknownMaxAttachment`() {
        val u = parse<MessageCreatedUpdate>("message_created_unknown_attachment")
        assertTrue(u.message.body.attachments!!.single() is UnknownMaxAttachment)
    }
}
