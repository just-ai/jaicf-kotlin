package com.justai.jaicf.core.test.processresult

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private val stateAssertionScenario = Scenario {
    state("first") {
        activators {
            regex("first")
        }
        action {
            reactions.go("/middle")
        }
    }

    state("middle") {
        action {
            reactions.go("/last")
        }
    }

    state("last") {
        action {
            reactions.say("i'm in last")
        }
    }
}


class StateAssertionTest : ScenarioTest(stateAssertionScenario) {

    @Test
    fun `should assert all states through scenario -- positive test`() {
        query("first") startsWithContext "/" goesToState "/first" visitsState "/middle" endsWithState "/last"
    }

    @Test
    fun `should assert all states through scenario -- negative test`() {
        val processResult = query("first")
        assertThrows<AssertionError> {
            processResult startsWithContext "/not ok"
        }

        assertThrows<AssertionError> {
            processResult goesToState "/not ok"
        }

        assertThrows<AssertionError> {
            processResult visitsState "/not ok"
        }

        assertThrows<AssertionError> {
            processResult endsWithState "/not ok"
        }
    }

    @Test
    fun `should assert all visited states through scenario -- positive test`() {
        query("first") visitsState "/first" visitsState "/middle" visitsState "/last"
    }
}