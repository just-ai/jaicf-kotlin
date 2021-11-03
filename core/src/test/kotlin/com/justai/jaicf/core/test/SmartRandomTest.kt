package com.justai.jaicf.core.test

import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DefaultActionContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.test.reactions.TestReactions
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmartRandomTest {

    private lateinit var context: DefaultActionContext

    @BeforeAll
    fun createContext() {
        context = ActionContext(
            BotContext("clientId"),
            StrictActivatorContext(),
            QueryBotRequest("", ""),
            TestReactions()
        )
    }

    @RepeatedTest(100, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `Smart random generates no duplicates`() {
        val (max, count) = 10 to 5
        val randoms = IntArray(count) { context.random(max) }

        assertTrue(randoms.distinct().size == count) { "Duplicated random value was found" }
    }

    @RepeatedTest(100, name = RepeatedTest.LONG_DISPLAY_NAME)
    fun `Smart random generates values within its bounds`() {
        val (max, count) = 10 to 100
        val randoms = IntArray(count) { context.random(max) }

        val range = 0 until max
        randoms.forEach { assertTrue(it in range) { "Expected value in range $range but was $it" } }
    }
}