package com.justai.jaicf.test.mockjvm.tests

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.files.Audio
import com.github.kotlintelegrambot.network.Response
import com.justai.jaicf.channel.telegram.TelegramAudioRequest
import com.justai.jaicf.channel.telegram.TelegramEvent
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.mockjvm.query
import com.justai.jaicf.test.mockjvm.request
import com.justai.jaicf.test.mockjvm.withChannel
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


private val scenario = object : Scenario() {
    init {
        state("first") {
            activators {
                regex("first")
                intent("first")
            }
            action(telegram) {
                reactions.say("first")
                reactions.say("second")
                reactions.sendMessage("text", disableWebPagePreview = false)
                reactions.sendPhoto("photo-url.url")
                reactions.go("nested")
            }

            state("nested") {
                action {
                    reactions.say("ok")
                }
            }
        }

        state("dynamic") {
            activators {
                regex("dynamic")
            }
            action(telegram) {
                reactions.say("dynamic")
                reactions.sendMessage("first")
                reactions.sendMessage("second")
                reactions.buttons("first", "second")
            }
        }

        state("apiCall") {
            activators {
                regex("apiCall")
            }
            action(telegram) {
                reactions.say("i'm bot: ${reactions.api.getMe().first?.body()?.result?.firstName}")
            }
        }

        state("withRequest") {
            activators {
                event(TelegramEvent.AUDIO)
            }
            action(telegram) {
                reactions.say("audio captured")
            }
        }
    }
}

class ChannelTokenTests : ScenarioTest(scenario.model) {

    @Test
    fun `should run with strict reactions`() = withChannel(telegram) {
        enforceStrictOrder = false
        useDefaultReactionsApi = false
        query("first") {
            say("first")
            say("second")
            calls { sendMessage("text", disableWebPagePreview = false) }
            calls { sendPhoto("photo-url.url") }
            go("nested")
            say("ok")
        }
    }

    @Test
    fun `should fail with invalid order`() = withChannel(telegram) {
        useDefaultReactionsApi = false

        assertThrows<AssertionError> { // TODO: proper exceptions
            query("first") {
                say("second")
                say("first")
                go("/first/nested")
                calls { sendMessage("text", disableWebPagePreview = false) }
                calls { sendPhoto("photo-url.url") }
                say("ok")
            }
        }
    }

    @Test
    fun `should answer with dynamic reaction`() = withChannel(telegram) {
        query("dynamic") {
            say("dynamic")
            callsDynamic { reactions.sendMessage(any()) }
            buttons("first", "second")
        }
    }

    @Test
    fun `should maintain strict order with dynamic reaction in the middle`() = withChannel(telegram) {
        useDefaultReactionsApi = false
        assertThrows<AssertionError> {
            query("dynamic") {
                callsDynamic { reactions.sendMessage(any()) }
                buttons("first", "second")
                say("dynamic")
            }
        }
    }

    @Test
    fun `should perform api call from reactions and use result`() = withChannel(telegram) {
        query("apiCall") {
            callsDynamicWithAnswer { reactions.api.getMe() } answers {
                retrofit2.Response.success(Response(User(0, true, "some-bot-name"), true, null, null)) to null
            }
            say("i'm bot: some-bot-name")
        }
    }

    @Test
    fun `should send channel request`() = withChannel(telegram) {
        val msg = mockk<Message>()
        every { msg.chat } returns Chat(0, "private")
        val req = TelegramAudioRequest(msg, Audio("id", "uniq", 10))

        request(req) {
            say("audio captured")
        }
    }
}
