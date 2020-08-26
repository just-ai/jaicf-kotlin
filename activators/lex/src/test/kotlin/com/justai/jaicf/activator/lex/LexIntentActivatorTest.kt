package com.justai.jaicf.activator.lex

import com.justai.jaicf.model.scenario.Scenario
import org.junit.jupiter.api.Test


class LexIntentActivatorTest : LexIntentActivatorBaseTest(TestScenario) {

    @Test
    fun `Activates when Lex is ready to fulfill this intent`() = botTest {
        respondReadyForFulfillment("intent")
        query("text") endsWithState "/main"
    }

    @Test
    fun `Doesn't activate when Lex is ready to fulfill another intent`() = botTest {
        respondReadyForFulfillment("different_intent")
        query("text") endsWithState "/fallback"
    }

    @Test
    fun `Activates when Lex tries to fill slots for this intent`() = botTest {
        respondElicitSlot("intent", slotToElicit = "slot")
        query("text") endsWithState "/main"
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


    object TestScenario : Scenario() {
        init {
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
    }
}