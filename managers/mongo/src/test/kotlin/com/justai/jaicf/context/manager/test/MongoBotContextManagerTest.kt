package com.justai.jaicf.context.manager.test

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.manager.mongo.MongoBotContextManager
import com.mongodb.client.MongoClients
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled

@Disabled
class MongoBotContextManagerTest {

    private val client = MongoClients.create("mongodb://test:testtest1@ds125385.mlab.com:25385/jaicf")
    private val manager = MongoBotContextManager(client.getDatabase("jaicf").getCollection("contexts"))

    @Test
    fun testSave() {
        val context = BotContext("client1").apply {
            result = "some result"
            session["key1"] = "some value"
        }

        manager.saveContext(context, null, null)

        val result = manager.loadContext(EventBotRequest("client1", "event"))

        assertNotNull(result)
        assertEquals(context.result, result.result)
        assertEquals(context.session, result.session)
    }

    @Test
    fun testSaveCustomBean() {
        val context = BotContext("client2").apply {
            result = CustomValue(1)
            session["value"] = CustomValue(CustomValue(2))
        }

        manager.saveContext(context, null, null)

        val result = manager.loadContext(EventBotRequest("client2", "event"))

        assertNotNull(result)
        assertTrue(result.result is CustomValue)
        assertTrue(result.session["value"] is CustomValue)
    }
}