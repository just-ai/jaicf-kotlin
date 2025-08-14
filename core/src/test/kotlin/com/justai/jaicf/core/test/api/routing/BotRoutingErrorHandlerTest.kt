package com.justai.jaicf.core.test.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

private fun createEngineWithFallback(answer: String) = BotEngine(Scenario {
    state("changeEngine", noContext = true) {
        activators { regex("changeEngine .*") }
        action { routing.changeEngine(request.input.replace("changeEngine ", "")) }
    }
    fallback { reactions.say(answer) }
}, activators = arrayOf(RegexActivator))

private val t2 = BotRoutingEngine(
    main = "t2-main" to createEngineWithFallback("t2-main"),
    routables = mapOf(
        "echo" to createEngineWithFallback("t2-echo")
    )
)

private val t1 = BotRoutingEngine(
    main = "t1-main" to createEngineWithFallback("t1-main"),
    routables = mapOf(
        "echo" to createEngineWithFallback("t1-echo"),
        "t2" to t2,
    )
)

@TestMethodOrder(MethodOrderer.MethodName::class)
class BotRoutingErrorHandlingTest : BotTest(t1) {

    @Test
    fun `01 should rollback to main engine on attempted transition to unknown engine`() {
        query("changeEngine unknown")
        query("test") responds "t1-main"
    }

    @Test
    fun `02 should select mainEngine for nested router`() {
        query("changeEngine t2-main")
        query("changeEngine unknown")
        query("test") responds "t2-main"
    }
}
