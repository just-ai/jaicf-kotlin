package com.justai.jaicf.context.manager.test

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.mapdb.MapDbBotContextManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapDbBotContextManagerTest {

    @AfterAll
    internal fun shutdown() {
        Files.deleteIfExists(Paths.get(".mapdb"))
    }

    @Test
    fun testWithTempFile() {
        val manager = MapDbBotContextManager()
        val context = manager.loadContext(EventBotRequest("client1", "event"), RequestContext.DEFAULT)
        context.apply {
            result = "some result"
            session["key1"] = "value1"
        }

        manager.saveContext(context, null, null, RequestContext.DEFAULT)
        val result = manager.loadContext(EventBotRequest("client1", "event"), RequestContext.DEFAULT)

        assertEquals("client1", context.clientId)
        assertEquals(context.clientId, result.clientId)
        assertEquals(context.result, result.result)
        assertEquals(context.session, result.session)
    }

    @Test
    fun testWithFile() {
        val context = BotContext("client1").apply { result = "some result" }
        MapDbBotContextManager(".mapdb").apply {
            saveContext(context, null, null, RequestContext.DEFAULT)
            close()
        }

        val result = MapDbBotContextManager(".mapdb").run {
            loadContext(EventBotRequest(context.clientId, "event"), RequestContext.DEFAULT)
                .also { close() }
        }

        assertEquals(context.result, result.result)
    }
}