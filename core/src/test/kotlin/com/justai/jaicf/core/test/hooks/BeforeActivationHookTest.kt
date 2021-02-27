package com.justai.jaicf.core.test.hooks

import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.hook.BeforeActivationHook
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val scenario = object : Scenario() {
    init {
        handle<BeforeActivationHook> {
            it.setRequestType(BotRequestType.QUERY)
            it.setRequestInput(it.request.input.removePrefix("bad-prefix-"))
        }

        state("error") {
            activators {
                regex("some-query")
            }
            action {
                reactions.say("ok")
            }
        }
    }
}

class BeforeActivationHookTest : ScenarioTest(scenario.model) {

    @Test
    fun `should process as query request`() {
        event("some-query") responds "ok"
    }

    @Test
    fun `should modify input and process`() {
        event("bad-prefix-some-query") responds "ok"
    }
}
