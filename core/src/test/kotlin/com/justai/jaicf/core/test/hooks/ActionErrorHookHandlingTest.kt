package com.justai.jaicf.core.test.hooks

import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val scenario = object : Scenario() {
    init {
        handle<ActionErrorHook> {
            it.reactions.say("Error happened. Sorry.")
        }

        state("error") {
            activators { regex("error") }
            action {
                error("This is error")
            }
        }

        state("ok") {
            activators { regex("ok") }
            action {
                reactions.say("ok")
            }
        }
    }
}

class ActionErrorHookHandlingTest : ScenarioTest(scenario.model) {

    @Test
    fun `should handle error with some reaction`() {
        query("error") responds "Error happened. Sorry."
    }

    @Test
    fun `should not invoke error hook when no errors`() {
        query("ok") responds "ok"
    }

}


