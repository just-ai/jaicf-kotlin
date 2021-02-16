package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInIntentStatus
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
            reactions.say("ok", interruptable = true)
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
            reactions.say("i'm in InvalidContext", interruptInContext = "/InvalidContextPath")
        }
    }

    fallback {
        reactions.say("fallback")
    }
}

private val channel = JaicpTestChannel(scenario, TelephonyChannel)

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
        val data = BargeInIntentStatus(
            BargeInIntentStatus.BargeInTransition("."),
            BargeInIntentStatus.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).doesInterrupt()
    }

    @Test
    fun `should fail to interrupt with no state possible`() {
        val data = BargeInIntentStatus(
            BargeInIntentStatus.BargeInTransition("."),
            BargeInIntentStatus.RecognitionResult("some-unknown-state", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
    }

    @Test
    fun `should fail to interrupt with another context`() {
        val data = BargeInIntentStatus(
            BargeInIntentStatus.BargeInTransition("/ModalContext"),
            BargeInIntentStatus.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
    }

    @Test
    fun `should fail to interrupt into fallback`() {
        val data = BargeInIntentStatus(
            BargeInIntentStatus.BargeInTransition("/OpenContext"),
            BargeInIntentStatus.RecognitionResult("nested", "FINAL")
        ).toJson()

        query("start").answers("ok")
        event("bargeInIntent", "bargeInIntentStatus" to data).failsInterrupt()
    }

    @Test
    @Disabled("wait until #147 with AnyErrorHook is merged, test is also invalid btw")
    fun `should restore after invalid context path transition`() {
        val data = BargeInIntentStatus(
            BargeInIntentStatus.BargeInTransition("/InvalidContextPath"),
            BargeInIntentStatus.RecognitionResult("nested", "FINAL")
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

private fun BargeInIntentStatus.toJson() = JSON.encodeToJsonElement(serializer(), this)
