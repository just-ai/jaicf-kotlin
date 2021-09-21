package com.justai.jaicf.core.test.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.api.routing.routingContext
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


private fun createEngine(scenario: Scenario) = BotEngine(scenario, activators = arrayOf(RegexActivator))

private val main = Scenario {
    state("route sc2 and back") {
        activators {
            regex("route sc2 and back")
        }
        action {
            routing.route("sc2")
        }
    }

    state("route") {
        activators {
            regex("route .*")
        }
        action {
            val target = request.input.replace("route ", "")
            reactions.say("Routing current request to $target")
            routing.route(target)
        }
    }

    state("changeBot") {
        activators {
            regex("changeBot .*")
        }
        action {
            val target = request.input.replace("changeBot ", "")
            reactions.say("Changing bot to $target")
            routing.changeEngine(target)
        }
    }

    fallback {
        reactions.say("MAIN: Fallback")
    }
}

private val sc1 = Scenario {
    state("routeBack") {
        activators {
            regex("routeBack")
        }
        action {
            reactions.say("routing back")
            routing.routeBack()
        }
    }

    state("changeBotBack") {
        activators {
            regex("changeBotBack")
        }

        action {
            reactions.say("changing bot back")
            routing.changeEngineBack()
        }
    }

    state("changeBot") {
        activators {
            regex("changeBot .*")
        }
        action {
            val target = request.input.replace("changeBot ", "")
            reactions.say("Changing bot to $target")
            routing.changeEngine(target)
        }
    }

    state("changeBotWithTarget") {
        activators {
            regex("changeBotWithTarget")
        }
        action {
            reactions.say("Changing bot to sc3, target state is /target")
            routing.changeEngine("sc3", "/target")
        }
    }

    state("routeWithTarget") {
        activators {
            regex("routeWithTarget")
        }
        action {
            reactions.say("Routing to sc3, target state is /target")
            routing.route("sc3", "/target")
        }
    }

    fallback {
        reactions.say("SC1: Fallback")
    }
}

private val sc2 = Scenario {
    state("changeBotBack") {
        activators {
            regex("changeBotBack")
        }

        action {
            reactions.say("changing bot back")
            routing.changeEngineBack()
        }
    }

    fallback {
        reactions.say("SC2: Fallback")
    }
}

private val sc3 = Scenario {
    state("target") {
        state("child") {
            activators {
                regex("child")
            }
            action {
                reactions.say("sc3 target child")
            }
        }
    }
}

private val router = BotRoutingEngine(
    "main" to createEngine(main),
    mapOf("sc1" to createEngine(sc1), "sc2" to createEngine(sc2), "sc3" to createEngine(sc3))
)

@TestMethodOrder(MethodOrderer.Alphanumeric::class)
class BotRoutingApiTest : BotTest(router) {

    @Test
    fun `01 should answer in tests like a normal botEngine`() {
        query("Test") responds "MAIN: Fallback"
    }

    @Test
    fun `02 should have main engine in stack`() {
        query("Test")
        assertEquals(botContext.routingContext.routingStack.pop().toEngine, "main")
    }

    @Test
    fun `03 should route`() {
        query("route sc1") hasAnswer "Routing current request to sc1" hasAnswer "SC1: Fallback"
        assertEquals(botContext.routingContext.routingStack.pop().toEngine, "sc1")
    }

    @Test
    fun `04 should route and route back to main engine`() {
        query("route sc2 and back") responds "SC2: Fallback"
    }

    @Test
    fun `05 should changeBot sc1`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("Hello to sc1") responds "SC1: Fallback"
    }

    @Test
    fun `06 should changeBot and routeBack back sc1`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("routeBack") hasAnswer "routing back" hasAnswer "MAIN: Fallback"
    }

    @Test
    fun `07 should changeBot and changeBotBack back sc1`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("test") responds "SC1: Fallback"
        query("changeBotBack") responds "changing bot back"
        query("test") responds "MAIN: Fallback"
    }

    @Test
    fun `08 should changeBot several times and changeBotBack back to sc1`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("changeBot sc2") responds "Changing bot to sc2"
        query("test") responds "SC2: Fallback"
        query("changeBotBack") responds "changing bot back"
        query("test") responds "SC1: Fallback"
    }

    @Test
    fun `09 should changeEngine with target and be able to process next request`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("changeBotWithTarget")
        query("child") startsWithContext "/target" endsWithState "/target/child" responds "sc3 target child"
    }

    @Test
    fun `10 should changeEngine with target and be able to process next request`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("routeWithTarget")
        query("child") startsWithContext "/target" endsWithState "/target/child" responds "sc3 target child"
    }

}
