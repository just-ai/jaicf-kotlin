package com.justai.jaicf.channel.jaicp.channels

import com.justai.jaicf.BotEngine
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory.echoWithAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JaicpNativeChannelTests : JaicpBaseTest() {

    @Test
    fun `001 webhooks should answer chatapi`() {
        val channel = JaicpTestChannel(echoBot, ChatApiChannel)
        val response = channel.process(request)

        assertEquals(expected, response.jaicp)
    }

    @Test
    fun `002 webhooks should answer chatwidget`() {
        val channel = JaicpTestChannel(echoBot, ChatWidgetChannel)
        val response = channel.process(request)

        assertEquals(expected, response.jaicp)
    }

    @Test
    fun `003 webhooks should answer telephony`() {
        val channel = JaicpTestChannel(echoBot, TelephonyChannel)
        val response = channel.process(request)
        assertEquals(expected, response.jaicp)
    }
}

private val echoBot = BotEngine(echoWithAction {
    reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
}.model)

