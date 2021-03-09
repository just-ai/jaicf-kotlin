package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class HelloWorldScenarioTest: ScenarioTest(HelloWorldScenario) {

    @Test
    fun `Asks a name of each new user`() {
        query("hi") endsWithState "/helper/ask4name"
    }

    @Test
    fun `Greets a known user`() {
        withBotContext { client["name"] = "some name" }
        query("hi") endsWithState "/main"
    }

    @Test
    fun `Accepts any name`() {
        withCurrentContext("/helper")
        withBackState("/main/name")
        query("any name") goesToState "/helper/ask4name" endsWithState "/main/name"
    }

    @RepeatedTest(10)
    fun `Greets using user's name`() {
        withBotContext { client["name"] = "John" }
        query("hi") responds "Hello John!"
    }
}