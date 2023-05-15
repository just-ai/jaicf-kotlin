package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.BotEngine
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory
import com.justai.jaicf.channel.jaicp.ScenarioFactory.echoWithAction
import com.justai.jaicf.channel.jaicp.dto.TelephonySwitchMethod
import com.justai.jaicf.channel.jaicp.dto.TelephonySwitchReply
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInMode
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInTrigger
import com.justai.jaicf.channel.jaicp.reactions.telephony
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JaicpNativeChannelTests : JaicpBaseTest() {

    @Test
    fun `001 webhooks should answer chatapi`() {
        val channel = JaicpTestChannel(echoBot, ChatApiChannel)
        val response = channel.process(requestFromResources)

        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `002 webhooks should answer chatwidget`() {
        val channel = JaicpTestChannel(echoBot, ChatWidgetChannel)
        val response = channel.process(requestFromResources)

        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `003 webhooks should answer telephony`() {
        val channel = JaicpTestChannel(echoBot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
//    @Disabled("fix later")
    fun `004 webhooks should answer telephony with bargeIn`() {
        val bot = BotEngine(
            echoWithAction {
                reactions.telephony?.say(
                    "You said: ${request.input} from ${reactions::class.simpleName}",
                    bargeIn = true
                )
                reactions.telephony?.bargeIn(mode = BargeInMode.FORCED, trigger = BargeInTrigger.INTERIM, 101)
            }
        )

        val channel = JaicpTestChannel(bot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `005 webhooks should answer telephony with default transfer`() {
        val bot = BotEngine(
            ScenarioFactory.echoWithAction {
                reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
                reactions.telephony?.transferCall("79123456789")
            }
        )
        val channel = JaicpTestChannel(bot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        Assertions.assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `006 webhooks should answer telephony with REFER transfer`() {
        val bot = BotEngine(
            ScenarioFactory.echoWithAction {
                reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
                reactions.telephony?.transferCall(
                    TelephonySwitchReply(
                        phoneNumber = "79123456789",
                        method = TelephonySwitchMethod.REFER
                    )
                )
            }
        )
        val channel = JaicpTestChannel(bot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        Assertions.assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `007 webhooks should answer telephony with INVITE transfer`() {
        val bot = BotEngine(
            ScenarioFactory.echoWithAction {
                reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
                reactions.telephony?.transferCall(
                    TelephonySwitchReply(
                        phoneNumber = "79123456789",
                        method = TelephonySwitchMethod.INVITE
                    )
                )
            }
        )
        val channel = JaicpTestChannel(bot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        Assertions.assertEquals(responseFromResources, response.jaicp)
    }

    @Test
    fun `008 webhooks should answer telephony with sip URI`() {
        val bot = BotEngine(
            ScenarioFactory.echoWithAction {
                reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
                reactions.telephony?.transferCall(
                    TelephonySwitchReply(
                        sipUri = "123@123.org"
                    )
                )
            }
        )
        val channel = JaicpTestChannel(bot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        Assertions.assertEquals(responseFromResources, response.jaicp)
    }
}

private val echoBot = BotEngine(
    echoWithAction {
        reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
    }
)

