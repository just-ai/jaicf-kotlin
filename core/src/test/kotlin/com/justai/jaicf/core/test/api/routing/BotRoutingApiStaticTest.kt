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
    fallback {
        reactions.say("MAIN: Fallback")
    }
}

private val sc1 = Scenario {
    state("Dynamic") {
        activators {
            regex("Dynamic")
        }
        action {
            routing.route("sc2")

        }
    }

    fallback {
        reactions.say("SC1: Fallback")
    }
}


private val sc2 = Scenario {
    fallback {
        reactions.say("SC2: Fallback")
    }
}

private val router = BotRoutingEngine(
    createEngine(main),
    mapOf("sc1" to createEngine(sc1), "sc2" to createEngine(sc2)),
    staticRouteSelector = {
        when (request.input) {
            "/sc1" -> "sc1"
            "/sc2" -> "sc2"
            "/main" -> "main"
            else -> null
        }
    }
)

class BotRoutingApiStaticTest : BotTest(router) {

    @Test
    fun `should select routes statically`() {
        query("/sc1") responds "SC1: Fallback"
        query("something else") responds "SC1: Fallback"

        query("/sc2") responds "SC2: Fallback"
        query("something else") responds "SC2: Fallback"

        query("/main") responds "MAIN: Fallback"
        query("something else") responds "MAIN: Fallback"

    }

    @Test
    fun `should apply dynamic routing after static`() {
        query("/sc1") responds "SC1: Fallback"
        query("Dynamic") responds "SC2: Fallback"
    }

    @Test
    fun `should rest in statically selected bot`() {
        query("/sc1") responds "SC1: Fallback"
        query("something") responds "SC1: Fallback"
    }
}