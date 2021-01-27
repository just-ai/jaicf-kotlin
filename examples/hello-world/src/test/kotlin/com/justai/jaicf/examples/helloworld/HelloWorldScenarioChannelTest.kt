package com.justai.jaicf.examples.helloworld

import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.Slot
import com.justai.jaicf.channel.alexa.activator.AlexaIntentActivatorContext
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.channel.alexa.intent
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.mockjvm.*
import io.mockk.every
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class HelloWorldScenarioChannelTest : ScenarioTest(HelloWorldScenario) {

    @Test
    fun `should not ask for name if first name inferred from telegram`() = withChannel(telegram) {
        every { request.message.chat.firstName } answers { "First Name" }
        query("hi") endsWithState "/main"
    }

    @Test
    fun `Greets a known user`() = withChannel(telegram) {
        withBotContext { client["name"] = "some name" }
        query("hi") endsWithState "/main"
    }

    @Test
    fun `Accepts name from activator`() = withContext(alexa.intent) {
        withCurrentContext("/helper")
        withBackState("/main/name")

        val intentRequest = IntentRequest.builder()
            .withIntent(
                Intent.builder()
                    .withName("name")
                    .putSlotsItem("firstName", Slot.builder().withName("firstName").withValue("name").build())
                    .build()
            ).build()

        intent(AlexaIntentActivatorContext(intentRequest)) goesToState "/helper/ask4name" endsWithState "/main/name"
    }

    @RepeatedTest(10)
    fun `Greets using user's name`() = withChannel(telegram) {
        withBotContext { client["name"] = "John" }
        query("hi") responds "Hello John!"
    }
}