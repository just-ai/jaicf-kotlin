package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.TextReply
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInIntentType
import com.justai.jaicf.channel.jaicp.telephony
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private val scenario = Scenario {
    state("interruptable") {
        activators {
            regex("interruptable")
        }
        action(telephony) {
            reactions.say("this is fine im in state start", interruptable = true)
        }
    }

    state("interruptInContext") {
        activators {
            regex("interruptInContext")
        }
        action(telephony) {
            reactions.say("this is fine im in state start", interruptInContext = "/Some/Test/State")
        }
    }

}

private val channel = JaicpTestChannel(scenario, TelephonyChannel)

class BargeInIntentsDTOTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {

    @Test
    fun `should send correct dto with interruptable flag`() {
        val textReply = query("interruptable").responseData.parseReplies().filterIsInstance<TextReply>().first()
        assertEquals(".", textReply.bargeInReply?.bargeInTransition)
        assertEquals(BargeInIntentType.INTENT, textReply.bargeInReply?.bargeInIntent?.type)
    }

    @Test
    fun `should send correct dto with interruptInContext flag`() {
        val textReply = query("interruptInContext").responseData.parseReplies().filterIsInstance<TextReply>().first()
        assertEquals("/Some/Test/State", textReply.bargeInReply?.bargeInTransition)
        assertEquals(BargeInIntentType.INTENT, textReply.bargeInReply?.bargeInIntent?.type)
    }

    private fun query(query: String) = channel.process(commonRequestFactory.query(query))
}
