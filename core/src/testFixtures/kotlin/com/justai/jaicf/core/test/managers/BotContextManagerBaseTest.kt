package com.justai.jaicf.core.test.managers

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.Serializable

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BotContextManagerBaseTest {
    abstract val manager: BotContextManager

    @Test
    fun `Saves simple value`() {
        val context = BotContext("client1").apply {
            result = "some result"
            session["key1"] = "some value"
            client["key1"] = "some value"
        }

        val result = manager.exchangeContext(context)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(context.result, result.result)
        Assertions.assertEquals(context.session, result.session)
        Assertions.assertEquals(context.client, result.client)
    }

    @Test
    fun `Saves custom bean`() {
        val context = BotContext("client2").apply {
            result = CustomValue(1)
            session["value"] = CustomValue(CustomValue(2))
            client["value"] = CustomValue(CustomValue(2))
        }

        val result = manager.exchangeContext(context)

        Assertions.assertNotNull(result)
        Assertions.assertTrue(result.result is CustomValue)
        Assertions.assertTrue(result.session["value"] is CustomValue)
        Assertions.assertTrue(result.client["value"] is CustomValue)
    }

    @Test
    fun `Saves transition history`() {
        var context = BotContext("client3")
        assertEquals(listOf("/"), context.dialogContext.transitionHistory.toList())

        context.dialogContext.saveToTransitionHistory("/a")

        context = manager.exchangeContext(context)
        assertEquals(listOf("/", "/a"), context.dialogContext.transitionHistory.toList())

        context.dialogContext.saveToTransitionHistory("/a/b")

        context = manager.exchangeContext(context)
        assertEquals(listOf("/", "/a", "/a/b"), context.dialogContext.transitionHistory.toList())
    }

    @Test
    fun `Saves back states stack`() {
        var context = BotContext("client3")
        assertEquals(emptyList<String>(), context.dialogContext.backStateStack.toList())

        context.dialogContext.backStateStack.add("/a")

        context = manager.exchangeContext(context)
        assertEquals(listOf("/a"), context.dialogContext.backStateStack.toList())

        context.dialogContext.backStateStack.add("/a/b")

        context = manager.exchangeContext(context)
        assertEquals(listOf("/a", "/a/b"), context.dialogContext.backStateStack.toList())
    }

    @Test
    fun `Saves transitions`() {
        var context = BotContext("client3")
        assertEquals(emptyMap<String, String>(), context.dialogContext.transitions)

        context.dialogContext.transitions.put("a", "/a")

        context = manager.exchangeContext(context)
        assertEquals(mutableMapOf("a" to "/a"), context.dialogContext.transitions)

        context.dialogContext.transitions.put("b", "/b")

        context = manager.exchangeContext(context)
        assertEquals(mutableMapOf("a" to "/a", "b" to "/b"), context.dialogContext.transitions)
    }

    protected fun BotContextManager.saveContext(context: BotContext) = saveContext(context, null, null, RequestContext.DEFAULT)

    protected fun BotContextManager.loadContext(clientId: String) = loadContext(EventBotRequest(clientId, "event"), RequestContext.DEFAULT)

    protected fun BotContextManager.exchangeContext(context: BotContext): BotContext {
        val clientId = context.clientId
        saveContext(context)
        return loadContext(clientId)
    }

    data class CustomValue(val value: Any) : Serializable
}
