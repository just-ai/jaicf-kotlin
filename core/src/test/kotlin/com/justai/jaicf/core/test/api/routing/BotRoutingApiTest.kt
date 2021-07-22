package com.justai.jaicf.core.test.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.BotRoutingEngine
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.api.routing.routingContext
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


private fun createEngine(scenario: Scenario) = BotEngine(scenario, activators = arrayOf(RegexActivator))

private val main = Scenario {

    state("route sc1") {
        activators {
            regex("route sc1")
        }
        action {
            routing.route("sc1")
        }
    }

    state("changeBot sc1") {
        activators {
            regex("changeBot sc1")
        }
        action {
            reactions.say("Changing bot to sc1")
            routing.changeBot("sc1")
        }
    }
    state("route sc2 and back") {
        activators {
            regex("route sc2 and back")
        }
        action {
            routing.route("sc2")
        }
    }

    fallback {
        reactions.say("You said: ${request.input}")
    }
}

private val sc1 = Scenario {
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

class BotRoutingApiTest : BotTest(router) {

    @Test
    fun `should answer in tests like a normal botEngine`() {
        query("Test") responds "You said: Test"
    }

    @Test
    fun `should have main engine in stack`() {
        query("Test")
        assertEquals(botContext.routingContext.routingEngineStack[0], BotRoutingEngine.DEFAULT_ROUTE_NAME)
    }

    @Test
    fun `should route to sc1`() {
        query("route sc1") responds "SC1: Fallback"
    }

    @Test
    fun `should route and return to main engine`() {
        query("route sc2 and back") responds "SC2: Fallback"
    }

    @Test
    fun `should changeBot sc1`() {
        query("changeBot sc1") responds "Changing bot to sc1"
        query("Hello to sc1") responds "SC1: Fallback"
    }
}