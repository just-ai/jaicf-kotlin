package com.justai.jaicf.core.test

import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.helpers.action.smartRandom
import com.justai.jaicf.test.reactions.TestReactions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import java.util.*

class SmartRandomTest {

    private lateinit var context: DefaultActionContext

    @BeforeEach
    fun createContext() {
        context = ActionContext(
            BotContext(UUID.randomUUID().toString()),
            StrictActivatorContext(),
            QueryBotRequest("", ""),
            TestReactions()
        )
    }

    @RepeatedTest(100)
    fun testRandom() {
        val check = mutableListOf<Int>()
        for (i in 1..5) {
            val r = smartRandom(10, context)
            assertFalse(check.contains(r), "Element was found")
            check.add(r)
        }
    }
}