package com.justai.jaicf.core.test.activation

import com.justai.jaicf.builder.ScenarioBuilder
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val scenario = object : Scenario() {
    init {
        activatorState("some") {
            activatorState("current") {
                activatorState("state") {
                    activatorState("child") {}
                }
            }
            activatorState("another") {
                activatorState("state") {
                    activatorState("child") {
                        state("fromState"){
                            activators(fromState = "/some") {
                                regex("child")
                            }
                        }
                    }
                }
            }

            state("globalState") {
                globalActivators {
                    regex("child")
                }
            }
        }
    }
}

class ActivationScenarioTest : ScenarioTest(scenario) {

    @Test
    fun `should activate child`() {
        withBotContext { dialogContext.currentContext = "/some/current/state" }
        query("child") goesToState "/some/current/state/child"
    }

    @Test
    fun `should step up and activate`() {
        withBotContext { dialogContext.currentContext = "/some/current/state" }
        query("another") goesToState "/some/another"
    }

    @Test
    fun `should activate global activator state`() {
        withBotContext { dialogContext.currentContext = "/" }
        query("child") goesToState "/some/globalState"
    }

    @Test
    fun `should activate by fromState`() {
        withBotContext { dialogContext.currentContext = "/some" }
        query("child") goesToState "/some/another/state/child/fromState"
    }
}


private fun ScenarioBuilder.activatorState(pattern: String, body: ScenarioBuilder.StateBuilder.() -> Unit) =
    state(pattern) {
        activators {
            regex(pattern)
        }
        body.invoke(this)
    }
