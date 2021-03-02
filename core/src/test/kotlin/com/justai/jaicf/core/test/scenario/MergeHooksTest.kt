package com.justai.jaicf.core.test.scenario

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.RootBuilder
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.hook.BeforeActionHook
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class MergeHooksTest {

    private fun testScenario(name: String, init: RootBuilder<*, *>.() -> Unit = {}) = Scenario {
        init()

        state(name) {
            activators { intent(name) }
            action { reactions.say(name) }
        }
    }

    private fun runTest(scenario: Scenario, body: ScenarioTest.() -> Unit) {
        ScenarioTest(scenario).apply { init() }.apply(body)
    }

    private fun RootBuilder<*, *>.testHandle(list: MutableList<String>, name: String) {
        handle<BeforeActionHook> { list += "$name hook from ${state.path.toString()}" }
    }

    private fun List<String>.assertHooks(vararg results: String) = assertEquals(results.toList(), this)

    @Test
    fun `Top-level append without context without expose`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")
            append(helperScenario, exposeHooks = false)
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "helper" endsWithState "/helper"

            result.assertHooks(
                "test hook from /test",
                "test hook from /helper"
            )
        }
    }

    @Test
    fun `Top-level append without context with expose`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")
            append(helperScenario, exposeHooks = true)
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("helper") responds "helper" endsWithState "/helper"

            result.assertHooks(
                "test hook from /test",
                "helper hook from /test",
                "test hook from /helper",
                "helper hook from /helper"
            )
        }
    }

    @Test
    fun `Top-level append with context without expose with propagate`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")
            append(context = "Helper", helperScenario, exposeHooks = false, propagateHooks = true)

            state("route") {
                activators { intent("route") }
                action { reactions.go("/Helper") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("route") endsWithState "/Helper"
            intent("helper") responds "helper" endsWithState "/Helper/helper"

            result.assertHooks(
                "test hook from /test",
                "test hook from /route",
                "test hook from /Helper",
                "helper hook from /Helper",
                "test hook from /Helper/helper",
                "helper hook from /Helper/helper",
            )
        }
    }

    @Test
    fun `Top-level append with context without expose without propagate`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")
            append(context = "Helper", helperScenario, exposeHooks = false, propagateHooks = false)

            state("route") {
                activators { intent("route") }
                action { reactions.go("/Helper") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("route") endsWithState "/Helper"
            intent("helper") responds "helper" endsWithState "/Helper/helper"

            result.assertHooks(
                "test hook from /test",
                "test hook from /route",
                "helper hook from /Helper",
                "helper hook from /Helper/helper",
            )
        }
    }

    @Test
    fun `Top-level append with context with expose with propagate`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")
            append(context = "Helper", helperScenario, exposeHooks = true, propagateHooks = true)

            state("route") {
                activators { intent("route") }
                action { reactions.go("/Helper") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("route") endsWithState "/Helper"
            intent("helper") responds "helper" endsWithState "/Helper/helper"

            result.assertHooks(
                "test hook from /test",
                "helper hook from /test",
                "test hook from /route",
                "helper hook from /route",
                "test hook from /Helper",
                "helper hook from /Helper",
                "test hook from /Helper/helper",
                "helper hook from /Helper/helper"
            )
        }
    }

    @Test
    fun `Top-level append with context with expose without propagate`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")
            append(context = "Helper", helperScenario, exposeHooks = true, propagateHooks = false)

            state("route") {
                activators { intent("route") }
                action { reactions.go("/Helper") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("route") endsWithState "/Helper"
            intent("helper") responds "helper" endsWithState "/Helper/helper"

            result.assertHooks(
                "test hook from /test",
                "helper hook from /test",
                "test hook from /route",
                "helper hook from /route",
                "helper hook from /Helper",
                "helper hook from /Helper/helper"
            )
        }
    }


    @Test
    fun `Inner append without context`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")

            state("inner") {
                append(helperScenario)

                activators { intent("inner") }
                action { reactions.say("inner") }
            }
        }

        runTest(scenario) {
            intent("test") responds "test" endsWithState "/test"
            intent("inner") responds "inner" endsWithState "/inner"
            intent("helper") responds "helper" endsWithState "/inner/helper"

            result.assertHooks(
                "test hook from /test",
                "test hook from /inner",
                "test hook from /inner/helper",
            )
        }
    }

    @Test
    fun `Inner append with context with propagate`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")

            state("inner") {
                append(context = "Helper", helperScenario, propagateHooks = true)

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
            intent("inner") responds "inner" endsWithState "/inner"
            intent("route") endsWithState "/inner/Helper"
            intent("helper") responds "helper" endsWithState "/inner/Helper/helper"

            result.assertHooks(
                "test hook from /test",
                "test hook from /inner",
                "test hook from /inner/route",
                "test hook from /inner/Helper",
                "helper hook from /inner/Helper",
                "test hook from /inner/Helper/helper",
                "helper hook from /inner/Helper/helper"
            )
        }
    }

    @Test
    fun `Inner append with context without propagate`() {
        val result = mutableListOf<String>()

        val helperScenario = testScenario(name = "helper") {
            testHandle(result, "helper")
        }

        val scenario = testScenario(name = "test") {
            testHandle(result, "test")

            state("inner") {
                append(context = "Helper", helperScenario, propagateHooks = false)

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
            intent("inner") responds "inner" endsWithState "/inner"
            intent("route") endsWithState "/inner/Helper"
            intent("helper") responds "helper" endsWithState "/inner/Helper/helper"

            result.assertHooks(
                "test hook from /test",
                "test hook from /inner",
                "test hook from /inner/route",
                "helper hook from /inner/Helper",
                "helper hook from /inner/Helper/helper"
            )
        }
    }
}