package com.justai.jaicf.context.manager.test

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.manager.mapdb.MapDbBotContextManager
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class MapDbBotContextManagerTest {

    @Test
    fun testWithTempFile() {
        val manager = MapDbBotContextManager()
        val context = manager.loadContext("client1")
        context.apply {
            result = "some result"
            session["key1"] = "value1"
        }

        manager.saveContext(context)
        val result = manager.loadContext("client1")

        assertEquals("client1", context.clientId)
        assertEquals(context.clientId, result.clientId)
        assertEquals(context.result, result.result)
        assertEquals(context.session, result.session)
    }

    @Test
    fun testWithFile() {
        val context = BotContext("client1").apply { result = "some result" }
        MapDbBotContextManager(".mapdb").apply {
            saveContext(context)
            close()
        }

        val result = MapDbBotContextManager(".mapdb").run {
            loadContext(context.clientId)
        }

        assertEquals(context.result, result.result)
    }
}