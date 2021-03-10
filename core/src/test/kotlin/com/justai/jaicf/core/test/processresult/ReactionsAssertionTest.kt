package com.justai.jaicf.core.test.processresult

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private val reactionsAssertionScenario = Scenario {
    state("first") {
        activators {
            regex("first")
        }
        action {
            reactions.image("image")
            reactions.buttons("buttons")
            reactions.say("say")
            reactions.audio("audio")
        }
    }
}

class ReactionsAssertionTest : ScenarioTest(reactionsAssertionScenario) {

    @Test
    fun `should assert all reactions from scenario -- positive test`() {
        query("first") hasAnswer "say" hasImage "image" hasAudio "audio" hasButtons listOf("buttons")
    }

    @Test
    fun `should assert all reactions through scenario -- negative test`() {
        val processResult = query("first")
        assertThrows<AssertionError> {
            processResult hasAnswer "not say"
        }
        assertThrows<AssertionError> {
            processResult hasImage "not image"
        }
        assertThrows<AssertionError> {
            processResult hasAudio "not audio"
        }
        assertThrows<AssertionError> {
            processResult hasButtons listOf("not buttons")
        }
        assertThrows<AssertionError> {
            processResult hasButtons listOf()
        }
    }
}