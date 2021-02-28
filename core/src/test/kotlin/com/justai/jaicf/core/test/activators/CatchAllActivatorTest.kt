package com.justai.jaicf.core.test.activators

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val catchAllScenario = Scenario {
    fallback {
        reactions.say("Got it.")
    }
}

class CatchAllActivatorTest : ScenarioTest(catchAllScenario) {

    @Test
    fun `catch all should catch query`() {
        query("any query") goesToState "/fallback"
    }

    @Test
    fun `catch all should catch intent`() {
        intent("any query") goesToState "/fallback"
    }

    @Test
    fun `catch all should catch event`() {
        event("any query") goesToState "/fallback"
    }
}
