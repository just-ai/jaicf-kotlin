package com.justai.jaicf.channel.jaicp.livechat

import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory.echoWithAction
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.channel.jaicp.dto.LiveChatSwitchReply
import com.justai.jaicf.channel.jaicp.reactions.switchToOperator
import com.justai.jaicf.reactions.jaicp.jaicpAsync
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JaicpLiveChatsTest : JaicpBaseTest() {

    @Test
    fun `001 livechat should switch to operator`() {
        val message = "My First Message"
        val scenario = echoWithAction {
            reactions.jaicpAsync?.switchToOperator(message)
        }
        JaicpTestChannel(scenario, ChatWidgetChannel).process(request)

        val act = requireNotNull(connectorHttpRequestBody)
        val livechatRequest = JSON.decodeFromString(LiveChatInitRequest.serializer(), act)
        assertTrue { livechatRequest.switchData.firstMessage == message }
    }

    @Test
    fun `002 livechat should switch to operator with full DTO`() {
        val expected = LiveChatSwitchReply(
            firstMessage = "My first message",
            closeChatPhrases = listOf("close me", "/close"),
            appendCloseChatButton = true,
            oneTimeMessage = false
        )
        val scenario = echoWithAction {
            reactions.jaicpAsync?.switchToOperator(expected)
        }
        JaicpTestChannel(scenario, ChatWidgetChannel).process(request)

        val actual = JSON.decodeFromString(
            LiveChatInitRequest.serializer(),
            requireNotNull(connectorHttpRequestBody)
        ).switchData

        assertEquals(expected, actual)
    }
}