package com.justai.jaicf.core.test.scenario

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.RootBuilder
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.builder.append
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test


class ScenarioAppendTest {

    private fun testScenario(name: String, init: RootBuilder<*, *>.() -> Unit = {}) = Scenario {
        init()

        state(name) {
            activators { intent(name) }
            action { reactions.say(name) }
        }

        fallback(name = "fallback $name") {
            reactions.say("fallback $name")
        }
    }

    private fun runTest(scenario: Scenario, body: ScenarioTest.() -> Unit) {
        ScenarioTest(scenario).apply { init() }.apply(body)
    }

    @Test
    fun `Top-level append without context`() {
        val helperScenario = testScenario(name = "helper")

        val scenario = testScenario(name = "test") {
            append(helperScenario)
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "helper" endsWithState "/helper"
        }
    }

    @Test
    fun `Top-level append with context non-modal`() {
        val helperScenario = testScenario(name = "helper")

        val scenario = testScenario(name = "test") {
            append(context = "Helper", helperScenario)

            state("route") {
                activators { intent("route") }
                action { reactions.go("/Helper") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "fallback test" endsWithState "/test"
            intent("route") endsWithState "/Helper"
            intent("helper") responds "helper" endsWithState "/Helper/helper"
            intent("test") responds "test" endsWithState "/test"
        }
    }

    @Test
    fun `Top-level append with context modal`() {
        val helperScenario = testScenario(name = "helper")

        val scenario = testScenario(name = "test") {
            append(context = "Helper", helperScenario, modal = true)

            state("route") {
                activators { intent("route") }
                action { reactions.go("/Helper") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "fallback test" endsWithState "/test"
            intent("route") endsWithState "/Helper"
            intent("helper") responds "helper" endsWithState "/Helper/helper"
            intent("test") responds "fallback helper" endsWithState "/Helper/helper"
        }
    }

    @Test
    fun `Inner append without context`() {
        val helperScenario = testScenario(name = "helper")

        val scenario = testScenario(name = "test") {
            state("inner") {
                append(helperScenario)

                activators { intent("inner") }
                action { reactions.say("inner") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "fallback test" endsWithState "/test"
            intent("inner") responds "inner" endsWithState "/inner"
            intent("helper") responds "helper" endsWithState "/inner/helper"
            intent("test") responds "test" endsWithState "/test"
        }
    }

    @Test
    fun `Inner append with context non-modal`() {
        val helperScenario = testScenario(name = "helper")

        val scenario = testScenario(name = "test") {
            state("inner") {
                append(context = "Helper", helperScenario)

                activators { intent("inner") }
                action { reactions.say("inner") }

                state("route") {
                    activators { intent("route") }
                    action { reactions.go("../Helper") }
                }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "fallback test" endsWithState "/test"
            intent("inner") responds "inner" endsWithState "/inner"
            intent("helper") responds "fallback test" endsWithState "/inner"
            intent("route") endsWithState "/inner/Helper"
            intent("helper") responds "helper" endsWithState "/inner/Helper/helper"
            intent("test") responds "test" endsWithState "/test"
        }
    }

    @Test
    fun `Inner append with context modal`() {
        val helperScenario = testScenario(name = "helper")

        val scenario = testScenario(name = "test") {
            state("inner") {
                append(context = "Helper", helperScenario, modal = true)

                activators { intent("inner") }
                action { reactions.say("inner") }

                state("route") {
                    activators { intent("route") }
                    action { reactions.go("../Helper") }
                }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "fallback test" endsWithState "/test"
            intent("inner") responds "inner" endsWithState "/inner"
            intent("helper") responds "fallback test" endsWithState "/inner"
            intent("route") endsWithState "/inner/Helper"
            intent("helper") responds "helper" endsWithState "/inner/Helper/helper"
            intent("test") responds "fallback helper" endsWithState "/inner/Helper/helper"
        }
    }

    @Test
    fun `Infix append`() {
        val firstScenario = testScenario("first")
        val secondScenario = testScenario("second")

        val scenario = firstScenario append secondScenario

        runTest(scenario) {
            intent("first") responds "first" endsWithState "/first"
            intent("second") responds "second" endsWithState "/second"
        }
    }
}