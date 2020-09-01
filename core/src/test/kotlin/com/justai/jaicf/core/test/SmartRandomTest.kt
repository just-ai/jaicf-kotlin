package com.justai.jaicf.core.test

import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.api.TextResponse
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.helpers.action.smartRandom
import com.justai.jaicf.reactions.TextReactions
import org.junit.jupiter.api.BeforeEach
import java.util.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.RepeatedTest

class SmartRandomTest {

    private lateinit var context: ActionContext

    @BeforeEach
    fun createContext() {
        context = ActionContext(
            BotContext(UUID.randomUUID().toString()),
            StrictActivatorContext(),
            QueryBotRequest("", ""),
            TextReactions(TextResponse()),
            listOf(),
            LoggingContext()
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