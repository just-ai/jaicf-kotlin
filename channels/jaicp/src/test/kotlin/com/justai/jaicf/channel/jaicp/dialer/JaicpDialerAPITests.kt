package com.justai.jaicf.channel.jaicp.dialer

import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory.echoWithAction
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.AllowedTime
import com.justai.jaicf.channel.jaicp.dto.LocalTimeInterval
import com.justai.jaicf.channel.jaicp.reactions.telephony
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import kotlin.test.assertEquals

internal class JaicpDialerAPITests : JaicpBaseTest() {

    @Test
    fun `001 dialer should answer with deprecated redial`() {
        val scenario = echoWithAction {
            val startRedialTime = Instant.ofEpochMilli(1605189536000)
            reactions.telephony?.redial(
                startDateTime = startRedialTime,
                finishDateTime = startRedialTime.plusSeconds(60),
                allowedDays = listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY),
                localTimeFrom = "12:00",
                localTimeTo = "12:30",
                maxAttempts = 5,
                retryIntervalInMinutes = 5
            )
            reactions.say("You said: ${request.input}")
        }

        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `002 dialer should answer with redial accepting allowedTime argument`() {
        val scenario = echoWithAction {
            val startRedialTime = Instant.ofEpochMilli(1605189536000)
            reactions.telephony?.redial(
                startDateTime = startRedialTime,
                finishDateTime = startRedialTime.plusSeconds(60),
                allowedDays = listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY),
                allowedTime = AllowedTime(
                    default = listOf(LocalTimeInterval("12:00", "12:30")),
                    wed = listOf(LocalTimeInterval("14:00", "15:30"))
                ),
                maxAttempts = 5,
                retryIntervalInMinutes = 5
            )
            reactions.say("You said: ${request.input}")
        }

        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `003 dialer should report property from call`() {
        val scenario = echoWithAction {
            reactions.telephony?.report("propname", "propvalue")
            reactions.say("You said: ${request.input}")
        }

        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `004 dialer should set call result`() {
        val scenario = echoWithAction {
            reactions.telephony?.setResult(
                callResult = "call ended",
                callResultPayload = buildJsonObject {
                    put("result", "Ok")
                }.toString()
            )
            reactions.say("You said: ${request.input}")
        }

        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `005 dialer should set merge all methods`() {
        val scenario = echoWithAction {
            val startRedialTime = Instant.ofEpochMilli(1605189536000)
            reactions.telephony?.redial(startRedialTime, null, maxAttempts = 5)
            reactions.telephony?.report("smoking", "i love it")
            reactions.telephony?.setResult(
                callResult = "call ended",
                callResultPayload = buildJsonObject {
                    put("result", "Ok")
                }.toString()
            )
            reactions.say("You said: ${request.input}")
        }

        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `006 dialer should set no input timeout`() {
        val scenario = echoWithAction {
            reactions.telephony?.noInputTimeout(15000)
            reactions.say("You said: ${request.input}")
        }

        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }
}