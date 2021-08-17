package com.justai.jaicf.context.manager.test

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.mapdb.JacksonMapDbBotContextManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JacksonMapDbBotContextManagerTest {

    @Test
    fun testWithTempFile() {
        val manager = JacksonMapDbBotContextManager()
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
        JacksonMapDbBotContextManager(".mapdb").apply {
            saveContext(context, null, null, RequestContext.DEFAULT)
            close()
        }

        val result = JacksonMapDbBotContextManager(".mapdb").run {
            loadContext(EventBotRequest(context.clientId, "event"), RequestContext.DEFAULT)
        }

        assertEquals(context.result, result.result)
    }
}
