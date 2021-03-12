package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInRequest
import com.justai.jaicf.channel.jaicp.scenario.BargeInProcessor
import com.justai.jaicf.channel.jaicp.telephony
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    fallback {
        reactions.say("fallback")
    }
}

private val channel = JaicpTestChannel(scenario, TelephonyChannel.Factory(BargeInProcessor.NON_FALLBACK))

class BargeInIntentsTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {

    @Test
    fun `query requests should not trigger any barge-in-intent interruption handlers`() {
        query("start").answers("ok")
    }

    @Test
    fun `should not mess with other states activation`() {
        event("some-event").answers("fallback")
    }

    @Test
    fun `should interrupt query with nested state`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("."),
            BargeInRequest.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).doesInterrupt()
    }

    @Test
    fun `should fail to interrupt with no state possible`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("."),
            BargeInRequest.RecognitionResult("some-unknown-state", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
    }

    @Test
    fun `should fail to interrupt with another context`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("/ModalContext"),
            BargeInRequest.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
    }

    @Test
    fun `should fail to interrupt into fallback`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("/OpenContext"),
            BargeInRequest.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
    }

    @Test
    @Disabled("wait until #147 with AnyErrorHook is merged, test is also invalid btw")
    fun `should restore after invalid context path transition`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("/InvalidContextPath"),
            BargeInRequest.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("InvalidContext").answers("i'm in InvalidContext")

        assertThrows<IllegalStateException> {
            event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
        }.apply { printStackTrace() }

        query("/start").answers("ok")
    }

    private fun query(query: String) = channel.process(commonRequestFactory.query(query))

    private fun event(event: String) = channel.process(commonRequestFactory.event(event))

    private fun event(event: String, additionalData: Pair<String, JsonElement>) =
        channel.process(commonRequestFactory.event(event, additionalData))
}

private fun BargeInRequest.toJson() = JSON.encodeToJsonElement(serializer(), this)
