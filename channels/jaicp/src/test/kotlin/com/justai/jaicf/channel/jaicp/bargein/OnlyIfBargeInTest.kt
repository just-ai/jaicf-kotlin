package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpResponseData
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInRequest
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test

val onlyIfBargeInTestScenario = Scenario {
    state("always enabled") {
        activators {
            regex("always enabled")
        }

        action {
            reactions.say("always enabled")
        }
    }

    state("barge in only") {
        activators {
            regex("barge in only").onlyIfBargeIn()
        }

        action {
            reactions.say("barge in only")
        }
    }

    state("barge in disabled") {
        activators {
            regex("barge in disabled").disableIfBargeIn()
        }

        action {
            reactions.say("barge in disabled")
        }
    }

    fallback {
        reactions.say("fallback")
    }
}

private val channel = JaicpTestChannel(onlyIfBargeInTestScenario, TelephonyChannel.Factory(BargeInProcessor.NON_FALLBACK))

class OnlyIfBargeInTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {
    @Test
    fun `Should activate always enabled without barge-in`() {
        query("always enabled").answers("always enabled")
    }

    @Test
    fun `Should activate always enabled with barge-in`() {
        bargeInQuery("always enabled").answers("always enabled")
    }

    @Test
    fun `Should activate always enabled after barge-in`() {
        query("always enabled").answers("always enabled")
        bargeInQuery("always enabled").answers("always enabled")
        query("always enabled").answers("always enabled")
        bargeInQuery("always enabled").answers("always enabled")
    }

    @Test
    fun `Should not activate barge in only without barge-in`() {
        query("barge in only").answers("fallback")
    }

    @Test
    fun `Should activate barge in only with barge-in`() {
        bargeInQuery("barge in only").answers("barge in only")
    }

    @Test
    fun `Should not activate barge in only after barge-in`() {
        query("barge in only").answers("fallback")
        bargeInQuery("barge in only").answers("barge in only")
        query("barge in only").answers("fallback")
        bargeInQuery("barge in only").answers("barge in only")
    }

    @Test
    fun `Should activate barge in disabled only without barge-in`() {
        query("barge in disabled").answers("barge in disabled")
    }

    @Test
    fun `Should not activate barge in disabled with barge-in`() {
        bargeInQuery("barge in disabled").failsInterrupt()
    }

    @Test
    fun `Should activate barge in disabled after barge-in`() {
        query("barge in disabled").answers("barge in disabled")
        bargeInQuery("barge in disabled").failsInterrupt()
        query("barge in disabled").answers("barge in disabled")
        bargeInQuery("barge in disabled").failsInterrupt()
    }

    fun bargeInQuery(input: String): HttpBotResponse {
        val bargeInReqest = commonRequestFactory.event("bargeInEvent", "bargeInIntentStatus" to BargeInRequest(
            BargeInRequest.BargeInTransition("."),
            BargeInRequest.RecognitionResult(input, "FINAL")
        ).toJson())

        val bargeInResponse = channel.process(bargeInReqest)
        if (bargeInResponse.jaicp.responseData.bargeInInterrupt?.interrupt == true) {
            val queryRequest = commonRequestFactory.query(input)
            return channel.process(queryRequest)
        }
        return bargeInResponse
    }

    private val JaicpBotResponse.responseData: JaicpResponseData get() =
        logger.info(JSON.encodeToString(JaicpBotResponse.serializer(), this)).let {
            JSON.decodeFromJsonElement(data)
        }

    private fun BargeInRequest.toJson() = JSON.encodeToJsonElement(serializer(), this)

    private fun query(query: String) = channel.process(commonRequestFactory.query(query))
}