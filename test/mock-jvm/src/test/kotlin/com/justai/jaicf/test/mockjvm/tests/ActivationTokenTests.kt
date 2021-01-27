package com.justai.jaicf.test.mockjvm.tests

import com.justai.jaicf.activator.caila.CailaIntentActivatorContext
import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.mockjvm.JSON
import com.justai.jaicf.test.mockjvm.intent
import com.justai.jaicf.test.mockjvm.withActivation
import org.junit.jupiter.api.Test


private val scenario = object : Scenario() {
    init {
        state("caila") {
            activators {
                intent("caila")
            }
            action(caila) {
                reactions.say("I'm in state caila")
            }
        }

        state("cailaWithContext") {
            activators {
                intent("cailaWithContext")
            }
            action {
                reactions.say("I'm in state cailaWithContext")
            }
        }
    }
}

class ActivationTokenTests : ScenarioTest(scenario.model) {

    @Test
    fun `should process activation in scenario`() = withActivation(caila) {
        intent("caila") goesToState "/caila" responds "I'm in state caila"
    }

    @Test
    fun `should process activation context in scenario`() = withActivation(caila) {
        val response = JSON.decodeFromString(
            CailaAnalyzeResponseData.serializer(),
            this::class.java.getResource("/caila.json").readText()
        )
        val ctx = CailaIntentActivatorContext(response, response.inference.variants.first())

        intent(ctx) goesToState "/cailaWithContext"
    }
}
