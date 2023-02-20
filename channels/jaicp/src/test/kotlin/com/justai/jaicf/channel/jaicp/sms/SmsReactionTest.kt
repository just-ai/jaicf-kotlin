package com.justai.jaicf.channel.jaicp.sms

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.ProviderConfig
import com.justai.jaicf.channel.jaicp.dto.SmsReply
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.channel.jaicp.telephony
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val scenario = Scenario(telephony) {
    state("sms") {
        activators {
            regex("sms")
        }
        action {
            reactions.telephony?.sendSms(
                "Test",
                "79999999999",
                ProviderConfig("I_DIGITAL", "12345", "Qwerty", "Just AI")
            )
        }
    }
}

private val channel = JaicpTestChannel(scenario, TelephonyChannel)

internal class SmsReplyTest : JaicpBaseTest(useCommonResources = true, ignoreSessionId = false) {

    @Test
    fun `sms reactions`() {
        val smsReply = query("sms").responseData.parseReplies().filterIsInstance<SmsReply>().first()
        assertNotNull(smsReply)
        assertEquals("79999999999", smsReply.phoneNumber)
        assertEquals("Just AI", smsReply.provider?.source)
    }

    private fun query(query: String) = channel.process(commonRequestFactory.query(query))
}