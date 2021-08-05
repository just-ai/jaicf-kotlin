package com.justai.jaicf.core.test.scenario

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

val modalTestScenario = Scenario {
    state("start") {
        activators { regex("start") }
        action { reactions.say("start") }

        state("modal", modal = true) {
            activators { regex("modal") }
            action { reactions.say("modal") }

            state("inner") {
                activators { regex("inner") }
                action { reactions.say("inner") }
            }
        }

        state("sibling") {
            activators { regex("sibling") }
            action { reactions.say("sibling") }
        }
    }
}

class ScenarioModalityTest : ScenarioTest(modalTestScenario) {
    @Test
    fun `should activate inner from modal`() {
        query("start") endsWithState "/start" hasAnswer "start"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
        query("inner") endsWithState "/start/modal/inner" hasAnswer "inner"
    }

    @Test
    fun `should activate modal from inner`() {
        query("start") endsWithState "/start" hasAnswer "start"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
        query("inner") endsWithState "/start/modal/inner" hasAnswer "inner"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
    }

    @Test
    fun `should activate modal from modal`() {
        query("start") endsWithState "/start" hasAnswer "start"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
    }

    @Test
    fun `should not activate parent from modal`() {
        query("start") endsWithState "/start" hasAnswer "start"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
        val result = query("start") endsWithState "/start/modal"
        assertTrue(result.reactionList.isEmpty())
    }

    @Test
    fun `should not activate sibling from modal`() {
        query("start") endsWithState "/start" hasAnswer "start"
        query("modal") endsWithState "/start/modal" hasAnswer "modal"
        val result = query("sibling") endsWithState "/start/modal"
        assertTrue(result.reactionList.isEmpty())
    }
}