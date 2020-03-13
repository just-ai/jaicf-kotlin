package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.Test

class HelloWorldBotTest: BotTest(helloWorldBot) {

    @Test
    fun `NLU extracts the name from a raw query`() {
        withCurrentContext("/helper")
        query("my name is john") returnsResult "John"
    }
}