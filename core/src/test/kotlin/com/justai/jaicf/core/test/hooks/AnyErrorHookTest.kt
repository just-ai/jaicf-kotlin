package com.justai.jaicf.core.test.hooks

import com.justai.jaicf.BotEngine
import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.hook.ActionErrorHook
import com.justai.jaicf.hook.AnyErrorHook
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

private val scenarioWithAnyErrorHook = object : Scenario() {
    init {
        handle<AnyErrorHook> {
            it.reactions.say("anyError")
        }

        handle<ActionErrorHook> {
            it.reactions.say("actionError")
        }

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

class AnyErrorHookTest : ScenarioTest(scenarioWithAnyErrorHook.model) {

    @Test
    fun `should handle noStateFound exception with anyError hook`() {
        query("goToUnknownState") responds "anyError"
    }

    @Test
    fun `should not invoke error hook when no errors`() {
        query("ok") responds "ok"
    }

}

fun main() {
    ConsoleChannel(BotEngine(scenarioWithAnyErrorHook.model)).run("/start")
}