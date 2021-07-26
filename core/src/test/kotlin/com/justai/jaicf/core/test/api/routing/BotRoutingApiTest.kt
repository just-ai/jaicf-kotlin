package com.justai.jaicf.core.test.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.api.routing.routingContext
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.assertEquals


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
            routing.changeBot(target)
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
            routing.changeBotBack()
        }
    }

    fallback {
        reactions.say("SC1: Fallback")
    }
}

private val sc2 = Scenario {
    fallback {
        reactions.say("SC2: Fallback")
        routing.changeBotBack()
    }
}

private val router = BotRoutingEngine(
    createEngine(main),
    mapOf("sc1" to createEngine(sc1), "sc2" to createEngine(sc2))
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
        assertEquals(botContext.routingContext.routingEngineStack[0], BotRoutingEngine.DEFAULT_ROUTE_NAME)
    }

    @Test
    fun `03 should route`() {
        query("route sc1") hasAnswer "Routing current request to sc1" hasAnswer "SC1: Fallback"
        assertEquals(botContext.routingContext.routingEngineStack[1], "sc1")
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
}