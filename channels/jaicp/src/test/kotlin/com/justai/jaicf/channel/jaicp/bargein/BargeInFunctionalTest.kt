package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInRequest
import com.justai.jaicf.channel.jaicp.telephony
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test

private val scenario = Scenario {
    state("start") {
        activators {
            regex("start")
        }
        action(telephony) {
            reactions.say("ok", bargeIn = true)
        }

        state("nested") {
            activators {
                regex("nested")
            }
            action {
                reactions.say("i'm in nested")
            }
        }
    }

    state("bargeInIntent") {
        activators {
            event("bargeInIntent")
        }
        action {
            reactions.say("I'm in state bargeInIntent")
        }
    }

    state("ModalContext", modal = true) {
        activators {
            regex("interrupt me")
        }
        action {
            reactions.say("interrupted")
        }
    }

    state("OpenContext") {
        activators {
            regex("interrupt me")
        }
        action {
            reactions.say("interrupted")
        }
    }

    state("InvalidContext") {
        activators {
            regex("InvalidContext")
        }
        action(telephony) {
            reactions.say("i'm in InvalidContext", bargeInContext = "/InvalidContextPath")
        }
    }

    state("startContext") {
        activators {
            regex("startContext")
        }

        action(telephony) {
            reactions.say("ok", bargeInContext = "/bargeInContext")
        }

        state("startNested") {
            activators {
                regex("startNested")
            }

            action {
                reactions.say("startNested")
            }
        }
    }

    state("bargeInContext", modal = true) {
        state("nestedNoContext", noContext = true, modal = true) {
            activators {
                regex("nestedNoContext")
            }

            action {
                reactions.say("nestedNoContext")
            }
        }

        state("nested", modal = true) {
            activators {
                regex("nested")
            }

            action {
                reactions.say("nested")
            }
        }
    }

    fallback {
        reactions.say("fallback")
    }
}

private val channel = JaicpTestChannel(scenario, TelephonyChannel.Factory(BargeInProcessor.NON_FALLBACK))

class BargeInFunctionalTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {

    @Test
    fun `query requests should not trigger any barge-in interruption handlers`() {
        query("start").answers("ok")
    }

    @Test
    fun `should not mess with other states activation`() {
        event("some-event").answers("fallback")
    }

    @Test
    fun `should stay in original context if fails to interrupt`() = testBargeInContext("/bargeInContext") {
        start("startContext")
        bargeIn("random query").failsInterrupt()
        bargeIn("another random query").failsInterrupt()
        query("startNested").answers("startNested")
    }

    @Test
    fun `should stay in original context if there were no interruption`() = testBargeInContext("/bargeInContext") {
        start("startContext")
        query("startNested").answers("startNested")
    }

    @Test
    fun `should stay in nested state if does interrupt`() = testBargeInContext("/bargeInContext") {
        start("startContext")
        bargeIn("nested").doesInterrupt()
        query("nested").answers("nested")
        query("nestedNoContext").answers("")
    }

    @Test
    fun `should stay in barge-in context if state is no-context and does interrupt`() = testBargeInContext("/bargeInContext") {
        start("startContext")
        bargeIn("nestedNoContext").doesInterrupt()
        query("nestedNoContext").answers("nestedNoContext")
        query("nested").answers("nested")
    }

    @Test
    fun `should interrupt query with nested state`() = testBargeInContext(".") {
        start("start")
        bargeIn("nested").doesInterrupt()
    }

    @Test
    fun `should fail to interrupt with no state possible`() = testBargeInContext(".") {
        start("start")
        bargeIn("some-unknown-state").failsInterrupt()
    }

    @Test
    fun `should fail to interrupt with another context`() = testBargeInContext("/ModalContext") {
        start("start")
        bargeIn("nested").failsInterrupt()
    }

    @Test
    fun `should fail to interrupt into fallback`() = testBargeInContext("/OpenContext") {
        start("start")
        bargeIn("nested").failsInterrupt()
    }

    @Test
    fun `should restore after invalid context path transition`() = testBargeInContext("/InvalidContextPath") {
        query("InvalidContext").answers("i'm in InvalidContext")
        bargeIn("nested").failsInterrupt()
        query("start").answers("ok")
    }

    private fun query(query: String) = channel.process(commonRequestFactory.query(query))

    private fun event(event: String) = channel.process(commonRequestFactory.event(event))

    private fun event(event: String, additionalData: Pair<String, JsonElement>) =
        channel.process(commonRequestFactory.event(event, additionalData))

    private fun bargeInEvent(text: String, transition: String) = event(
        "bargeInEvent", "bargeInIntentStatus" to BargeInRequest(
            BargeInRequest.BargeInTransition(transition),
            BargeInRequest.RecognitionResult(text, "FINAL")
        ).toJson()
    )

    private fun testBargeInContext(bargeInContext: String = ".", body: BargeInTest.() -> Unit) = BargeInTest(bargeInContext).run(body)

    inner class BargeInTest(val bargeInContext: String) {
        fun start(query: String) = query(query).answers("ok")
        fun bargeIn(text: String, transition: String = bargeInContext) = bargeInEvent(text, transition)
    }
}

private fun BargeInRequest.toJson() = JSON.encodeToJsonElement(serializer(), this)
