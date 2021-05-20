package com.justai.jaicf.activator.lex

import com.justai.jaicf.builder.Scenario
import org.junit.jupiter.api.Test

class LexIntentActivatorTest : LexIntentActivatorBaseTest(testScenario) {

    @Test
    fun `Activates when Lex is ready to fulfill this intent`() = botTest {
        respondClose("intent")
        query("text") endsWithState "/main"
    }

    @Test
    fun `Doesn't activate when Lex is ready to fulfill another intent`() = botTest {
        respondClose("different_intent")
        query("text") endsWithState "/fallback"
    }

    @Test
    fun `Activates and fill slots when intent has slots`() = botTest {
        respondElicitSlot("intent", responseMessage = "prompt", slotToElicit = "slot")
        query("text") endsWithState "/" responds "prompt"

        respondElicitSlot("intent", responseMessage = "prompt_2", slotToElicit = "slot_2")
        query("text") endsWithState "/" responds "prompt_2"

        respondConfirm("intent", confirmationMessage = "confirm")
        query("text") endsWithState "/" responds "confirm"

        respondClose("intent", responseMessage = "ok")
        query("text") endsWithState "/main" responds "ok"
    }

    @Test
    fun `Doesn't activate when Lex tries to fill slots for another intent`() = botTest {
        respondElicitSlot("different_intent", slotToElicit = "slot")
        query("text") endsWithState "/fallback"
    }

    @Test
    fun `Doesn't activate when Lex can't recognize intent`() = botTest {
        respondElicitIntent()
        query("text") endsWithState "/fallback"
    }

    @Test
    fun `Doesn't activate when Lex failed to continue conversation`() = botTest {
        respondFailed()
        query("text") endsWithState "/fallback"
    }
}

val testScenario = Scenario {
    state("main") {
        activators {
            intent("intent")
        }
    }

    state("fallback") {
        activators {
            catchAll()
        }
    }
}
