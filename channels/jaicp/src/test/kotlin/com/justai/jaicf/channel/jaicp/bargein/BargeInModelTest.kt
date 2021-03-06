package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.TextReply
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInType
import com.justai.jaicf.channel.jaicp.telephony
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val scenario = Scenario(telephony) {
    state("bargeIn") {
        activators {
            regex("bargeIn")
        }
        action {
            reactions.say("this is fine im in state start", bargeIn = true)
            reactions.audio("http://url.com", bargeIn = true)
        }
    }

    state("bargeInContext") {
        activators {
            regex("bargeInContext")
        }
        action {
            reactions.say("this is fine im in state start", bargeInContext = "/Some/Test/State")
            reactions.audio("http://url.com", bargeInContext = "/Some/Test/State")
        }
    }

    state("mixedBargeIn") {
        activators {
            regex("mixedBargeIn")
        }
        action {
            reactions.say("phrase1") // should not barge in -> type: IGNORE
            reactions.say("phrase2", bargeIn = true) // should interrupt -> type: INTENT
            reactions.say("phrase3", bargeIn = false) // should not barge in -> type: IGNORE
        }
    }

}

private val channel = JaicpTestChannel(scenario, TelephonyChannel)

class BargeInModelTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {

    @Test
    fun `should send correct dto with bargeIn flag`() {
        val textReply = query("bargeIn").responseData.parseReplies().filterIsInstance<TextReply>().first()
        assertEquals(".", textReply.bargeInReply?.bargeInTransition)
        assertEquals(BargeInType.INTENT, textReply.bargeInReply?.bargeInIntent?.type)


        val audioReply = query("bargeIn").responseData.parseReplies().filterIsInstance<AudioReply>().first()
        assertEquals(".", audioReply.bargeInReply?.bargeInTransition)
        assertEquals(BargeInType.INTENT, audioReply.bargeInReply?.bargeInIntent?.type)
    }

    @Test
    fun `should send correct dto with bargeInContext flag`() {
        val textReply = query("bargeInContext").responseData.parseReplies().filterIsInstance<TextReply>().first()
        assertEquals("/Some/Test/State", textReply.bargeInReply?.bargeInTransition)
        assertEquals(BargeInType.INTENT, textReply.bargeInReply?.bargeInIntent?.type)

        val audioReply = query("bargeInContext").responseData.parseReplies().filterIsInstance<AudioReply>().first()
        assertEquals("/Some/Test/State", audioReply.bargeInReply?.bargeInTransition)
        assertEquals(BargeInType.INTENT, audioReply.bargeInReply?.bargeInIntent?.type)
    }

    @Test
    fun `should send correct dto with mixed bargeIn replies`() {
        val textReplies = query("mixedBargeIn").responseData.parseReplies().filterIsInstance<TextReply>()
        textReplies[0].apply {
            assertNotNull(bargeInReply)
            assertEquals(BargeInType.IGNORE, bargeInReply?.bargeInIntent?.type)
        }

        textReplies[1].apply {
            assertNotNull(bargeInReply)
            assertEquals(BargeInType.INTENT, bargeInReply?.bargeInIntent?.type)
        }

        textReplies[2].apply {
            assertNotNull(bargeInReply)
            assertEquals(BargeInType.IGNORE, bargeInReply?.bargeInIntent?.type)
        }
    }

    private fun query(query: String) = channel.process(commonRequestFactory.query(query))
}
