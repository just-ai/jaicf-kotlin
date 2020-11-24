package com.justai.jaicf.core.test.activators

import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val intentActivationScenario = object : Scenario() {
    init {
        state("intent") {
            activators {
                intent("/foo")
            }
        }

        state("theme") {
            activators {
                theme("/bar")
            }
        }

        fallback {  }
    }
}

class IntentActivatorTest : ScenarioTest(intentActivationScenario.model) {

    @Test
    fun `should activate on strict intent match only`() {
        intent("/foo") goesToState "/intent"
        intent("/foo/bar") goesToState "/fallback"
    }

    @Test
    fun `should activate on any intent with the same prefix`() {
        intent("/bar") goesToState "/theme"
        intent("/bar/baz") goesToState "/theme"
    }
}