package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.BotEngine
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory.echoWithAction
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInMode
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInTrigger
import com.justai.jaicf.channel.jaicp.reactions.telephony
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
    fun `004 webhooks should answer telephony with simple bargeIn`() {
        val bot = BotEngine(
            echoWithAction {
                reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
                reactions.telephony?.bargeIn(mode = BargeInMode.FORCED, trigger = BargeInTrigger.INTERIM, 0)
            }
        )

        val channel = JaicpTestChannel(bot, TelephonyChannel)
        val response = channel.process(requestFromResources)
        assertEquals(responseFromResources, response.jaicp)
    }
}

private val echoBot = BotEngine(
    echoWithAction {
        reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
    }
)

