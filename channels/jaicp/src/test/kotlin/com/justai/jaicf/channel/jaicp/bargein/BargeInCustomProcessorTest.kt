package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.bargeIn
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInRequest
import com.justai.jaicf.channel.jaicp.telephony
import com.justai.jaicf.hook.BeforeActivationHook
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test

private val scenario = Scenario {
    state("bargeInIntent") {
        activators {
            regex("activate my custom processor")
        }
        action(telephony) {
            request.bargeIn?.let {
                if (it.bargeInRequest.recognitionResult.text == "оператор") {
                    reactions.allowInterrupt()
                }
            }
        }
    }
}

private val customBargeInProcessor = object : BargeInProcessor() {
    override fun handleBeforeActivation(hook: BeforeActivationHook) {
        hook.setRequestInput("activate my custom processor")
    }
}

private val channel = JaicpTestChannel(scenario, TelephonyChannel.Factory(customBargeInProcessor))

class BargeInCustomProcessorTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {

    @Test
    fun `should maintain ability to handle bargeInIntent event without helper scenario -- positive case`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("."),
            BargeInRequest.RecognitionResult("оператор", "FINAL")
        ).toJson()

        event("activate my custom processor", "bargeInIntentStatus" to data).doesInterrupt()
    }

    @Test
    fun `should maintain ability to handle bargeInIntent event without helper scenario -- negative case`() {
        val data = BargeInRequest(
            BargeInRequest.BargeInTransition("."),
            BargeInRequest.RecognitionResult("совсем не оператор", "FINAL")
        ).toJson()

        event("activate my custom processor", "bargeInIntentStatus" to data).failsInterrupt()
    }

    private fun event(event: String, additionalData: Pair<String, JsonElement>) =
        channel.process(commonRequestFactory.event(event, additionalData))
}

private fun BargeInRequest.toJson() = JSON.encodeToJsonElement(serializer(), this)
