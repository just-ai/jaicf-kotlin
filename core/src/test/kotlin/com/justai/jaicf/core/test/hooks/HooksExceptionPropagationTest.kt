package com.justai.jaicf.core.test.hooks

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private val scenario = Scenario {

    state("error") {
        activators {
            regex("error")
        }
        action {
            error("This is error")
        }
    }
}

class HooksExceptionPropagationTest : ScenarioTest(scenario) {

    @Test
    fun `should throw exception in test`() {
        assertThrows<Exception> {
            query("error") responds "Error happened. Sorry."
        }
    }
}


