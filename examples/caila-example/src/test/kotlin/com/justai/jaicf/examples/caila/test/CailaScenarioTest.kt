package com.justai.jaicf.examples.caila.test

import com.justai.jaicf.examples.caila.CailaScenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

class CailaScenarioTest: ScenarioTest(CailaScenario) {

    @Test
    fun `Handles intents with prefix`() {
        intent("/FAQ/Pricing") endsWithState "/pricing"
        intent("/FAQ") endsWithState "/faq"
    }

    @Test
    fun `Handles anyIntent`() {
        intent("/Smalltalk") goesToState "/smalltalk"
    }
}