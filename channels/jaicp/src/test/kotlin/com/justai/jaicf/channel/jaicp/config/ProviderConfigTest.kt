package com.justai.jaicf.channel.jaicp.config

import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory.echoWithAction
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.config.AsrYandexConfig
import com.justai.jaicf.channel.jaicp.dto.config.TtsAimyvoiceConfig
import com.justai.jaicf.channel.jaicp.reactions.telephony
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ProviderConfigTest : JaicpBaseTest() {

    @Test
    fun `001 config tts provider`() {
        val scenario = echoWithAction {
            reactions.telephony?.setTtsConfig(TtsAimyvoiceConfig("Никита"))
        }
        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `002 config asr provider`() {
        val scenario = echoWithAction {
            reactions.telephony?.setAsrConfig(
                AsrYandexConfig(
                    model = "general:rc",
                    lang = "pt-BR",
                    numbersAsWords = true,
                    sensitivityReduction = true
                )
            )
        }
        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `003 config asr properties`() {
        val scenario = echoWithAction {
            reactions.telephony?.setAsrProperties(mapOf(
                "hints.eou_timeout" to "4s",
                "insight_models" to listOf("call_features")
            ))
        }
        val channel = JaicpTestChannel(scenario, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }
}