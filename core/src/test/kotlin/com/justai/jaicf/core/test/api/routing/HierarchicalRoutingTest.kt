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
    state("changeEngine") {
        activators {
            regex("changeEngine .*")
        }
        action {
            val target = request.input.replace("changeEngine ", "")
            reactions.say("Changing bot to $target")
            routing.changeEngine(target)
        }
    }

    state("changeEngineBack") {
        activators {
            regex("changeEngineBack")
        }
        action {
            routing.changeEngineBack()
        }
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

@TestMethodOrder(MethodOrderer.Alphanumeric::class)
class HierarchicalRoutingTest : BotTest(t1) {

    @Test
    fun `01 should answer in tests like a normal botEngine`() {
        query("test") responds "t1-main"

        query("changeEngine echo") responds "Changing bot to echo"
        query("test") responds "t1-echo"

        query("changeEngine t2-main") responds "Changing bot to t2-main"
        query("test") responds "t2-main"

        query("changeEngine echo") responds "Changing bot to echo"
        query("test") responds "t2-echo"

    }
}
