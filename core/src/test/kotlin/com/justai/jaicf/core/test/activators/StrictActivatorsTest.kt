package com.justai.jaicf.core.test.activators

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val strictActivationScenario = Scenario {
    state("activate") {
        activators {
            regex("activate")
        }
        action {
            reactions.buttons("test").toStates(listOf("/test"))
        }
    }

    state("test") {
        action {
            reactions.say("test is fine")
        }
    }

    state("notActivate") {
        activators {
            regex("notActivate")
        }
    }

    fallback { }
}

class StrictActivatorsTest : ScenarioTest(strictActivationScenario) {

    @Test
    fun `should activate strict transition`() {
        query("activate") goesToState "/activate"
        query("test") goesToState "/test"
    }

    @Test
    fun `should not activate strict transition`() {
        query("notActivate") goesToState "/notActivate"
        query("test") goesToState "/fallback"
    }

    @Test
    fun `should clear strict transitions on activation`() {
        query("activate") goesToState "/activate"
        query("notActivate") goesToState "/notActivate"
        query("test") goesToState "/fallback"

        query("activate") goesToState "/activate"
        query("test") goesToState "/test"
    }
}