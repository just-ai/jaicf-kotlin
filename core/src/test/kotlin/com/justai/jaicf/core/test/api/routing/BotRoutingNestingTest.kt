package com.justai.jaicf.core.test.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.api.routing.NoRouteBackException
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

private fun createEngineWithFallback(answer: String) = BotEngine(Scenario {
    handle<ActionErrorHook> {
        if (exception.exception is NoRouteBackException){
            reactions.say("No route back available")
        }
    }

    state("changeEngine", noContext = true) {
        activators {
            regex("changeEngine .*")
        }
        action {
            val target = request.input.replace("changeEngine ", "")
            reactions.say("Changing bot to $target")
            routing.changeEngine(target)
        }
    }

    state("changeEngineBack", noContext = true) {
        activators {
            regex("changeEngineBack")
        }
        action {
            routing.changeEngineBack()
            reactions.say("Changing engine back")
        }
    }

    state("parent") {
        activators { regex("parent") }
        action { reactions.say("parent") }

        state("child") {
            activators { regex("child") }
            action { reactions.say("child") }
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
class BotRoutingNestingTest : BotTest(t1) {

    @Test
    fun `01 should support nested bot routing engines`() {
        query("test") responds "t1-main"

        query("changeEngine echo") responds "Changing bot to echo"
        query("test") responds "t1-echo"

        query("changeEngine t2-main") responds "Changing bot to t2-main"
        query("test") responds "t2-main"

        query("changeEngine echo") responds "Changing bot to echo"
        query("test") responds "t2-echo"

        query("changeEngineBack") responds "Changing engine back"
        query("test") responds "t2-main"

        query("changeEngineBack") responds "Changing engine back"
        query("test") responds "t1-echo"

        query("changeEngineBack") responds "Changing engine back"
        query("test") responds "t1-main"
    }

    @Test
    fun `02 should keep dialogContext`() {
        query("test") responds "t1-main"
        query("parent") responds "parent"
        query("child") responds "child"

        query("changeEngine echo") responds "Changing bot to echo"
        query("test") responds "t1-echo"

        query("changeEngine t2-main") responds "Changing bot to t2-main"
        query("test") responds "t2-main"

        query("changeEngine t1-main") responds "Changing bot to t1-main"
        query("child") responds "child"
    }

    @Test
    fun `03 should change engines in non-linear routes`() {
        // get some routing stack
        query("changeEngine t2-main")
        query("test") responds "t2-main"

        query("changeEngine echo")
        query("test") responds "t2-echo"

        query("changeEngine t1-main")
        query("test") responds "t1-main"

        query("changeEngine echo")
        query("test") responds "t1-echo"

        // and get back
        query("changeEngineBack")
        query("test") responds "t1-main"

        query("changeEngineBack")
        query("test") responds "t2-echo"

        query("changeEngineBack")
        query("test") responds "t2-main"

        query("changeEngineBack")
        query("test") responds "t1-main"

        // check handler
        query("changeEngineBack") responds "No route back available"
        query("test") responds "t1-main"

    }
}
