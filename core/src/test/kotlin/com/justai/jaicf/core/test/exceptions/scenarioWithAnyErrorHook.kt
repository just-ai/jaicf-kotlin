package com.justai.jaicf.core.test.exceptions

import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val scenarioWithExceptions = object : Scenario() {
    init {
        state("error") {
            activators {
                regex("goToUnknownState")
            }
            action {
                reactions.go("unknownState")
            }
        }

        state("ok") {
            activators {
                regex("ok")
            }
            action {
                reactions.say("ok")
            }
        }
    }
}

class AnyErrorHookTest : ScenarioTest(scenarioWithExceptions.model) {

    @Test
    fun `should handle noStateFound exception with anyError hook`() {
        query("goToUnknownState")
    }

    @Test
    fun `should not invoke error hook when no errors`() {
        query("ok") responds "ok"
    }

}
