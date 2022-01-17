package com.justai.jaicf.core.test.managers

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import java.util.*

interface BotContextManagerTest {
    val manager: BotContextManager

    fun BotContextManager.saveContext(context: BotContext) = saveContext(context, null, null, RequestContext.DEFAULT)

    fun BotContextManager.loadContext(clientId: String = UUID.randomUUID().toString()) =
        loadContext(EventBotRequest(clientId, "event"), RequestContext.DEFAULT)

    fun BotContextManager.exchangeContext(context: BotContext): BotContext {
        val clientId = context.clientId
        saveContext(context)
        return loadContext(clientId)
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BotContextManagerBaseTest : BotContextManagerTest {

    @Nested
    inner class BotContextBase : BotContextBaseTest(manager)

    @Nested
    inner class BotContextDelegates : BotContextDelegatesTest(manager)

}
