package com.justai.jaicf.core.test.hooks

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.hook.BotRequestHook
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val withBeforeProcessHook = Scenario {
    handle<BotRequestHook> {
        setRequestInput(request.input.removePrefix("bad-prefix-"))
    }
    state("ok") {
        activators { regex("some-query") }
        action { reactions.say("ok") }
    }
}

class BeforeProcessHookTest : ScenarioTest(withBeforeProcessHook) {

    @Test
    fun `should modify input and process`() {
        query("bad-prefix-some-query") responds "ok"
    }
}
