package com.justai.jaicf.core.test.managers.inmemory

import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.core.test.managers.BotContextManagerBaseTest
import org.junit.jupiter.api.BeforeAll

class InMemoryBotContextManagerTest : BotContextManagerBaseTest() {
    override lateinit var manager: BotContextManager

    @BeforeAll
    fun setup() {
        manager = InMemoryBotContextManager
    }
}