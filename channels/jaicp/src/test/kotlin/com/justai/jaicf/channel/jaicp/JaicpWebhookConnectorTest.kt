package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.googleactions.ActionsFulfillment
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.jaicp.channels.ChatApiChannel
import com.justai.jaicf.channel.jaicp.channels.ChatWidgetChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class JaicpWebhookConnectorTest : BaseTest() {
    @Test
    fun `001 webhooks should answer chatapi`() {
        val channel = JaicpWebhookConnector(
            botApi = echoBot,
            channels = listOf(ChatApiChannel),
            accessToken = ""
        )

        val request = HttpBotRequest(
            stream = getResourceAsInputStream("query.json")
        )

        val response = channel.processSync(request)
        val expected = """{"replies":[{"type":"text","text":"You said: /start from ChatApiReactions"}]}"""
        assertEquals(expected, response)
    }

    @Test
    fun `002 webhooks should answer chatwidget`() {
        val channel = JaicpWebhookConnector(
            botApi = echoBot,
            channels = listOf(ChatWidgetChannel),
            accessToken = ""
        )
        val request = HttpBotRequest(
            stream = getResourceAsInputStream("query.json")
        )

        val response = channel.processSync(request)
        val expected = """{"replies":[{"type":"text","text":"You said: /start from ChatWidgetReactions"}]}"""
        assertEquals(expected, response)
    }

    @Test
    fun `003 webhooks should answer telephony`() {
        val channel = JaicpWebhookConnector(
            botApi = echoBot,
            channels = listOf(TelephonyChannel),
            accessToken = ""
        )
        val request = HttpBotRequest(
            stream = getResourceAsInputStream("query.json")
        )
        val response = channel.processSync(request)
        val expected = """{"replies":[{"type":"text","text":"You said: /start from TelephonyReactions"}]}"""
        assertEquals(expected, response)
    }

    @Test
    fun `004 webhooks should async answer facebook`() {
        val channel = JaicpWebhookConnector(
            botApi = echoBot,
            channels = listOf(JaicpAsyncChannelMock.FB),
            accessToken = ""
        )
        val request = HttpBotRequest(
            stream = getResourceAsInputStream("query.json")
        )
        val response = channel.processAsync(request)
        val expected =
            """MessagePayload(super=Payload(recipient=IdRecipient(id=2993394024045339)), messagingType=RESPONSE, message=TextMessage(super=Message(quickReplies=Optional.empty, metadata=Optional.empty), text=You said: hello from FacebookReactionsMock), notificationType=Optional.empty, tag=Optional.empty)"""
        assertEquals(expected, response)
    }

    @Test
    fun `005 webhooks should answer google actions SDK`() {
        val channel = JaicpWebhookConnector(
            botApi = echoBot,
            channels = listOf(ActionsFulfillment.ActionsFulfillmentSDK),
            accessToken = ""
        )
        val request = HttpBotRequest(
            stream = getResourceAsInputStream("query.json")
        )
        val response = channel.processSync(request)
        val expected =
            """{"replies":[{"type":"raw","text":{"conversationToken":"{\"data\":{}}","expectUserResponse":true,"expectedInputs":[{"inputPrompt":{"richInitialPrompt":{"items":[{"simpleResponse":{"displayText":"You said: actions.intent.MAIN from ActionsReactions","ssml":"<speak>You said: actions.intent.MAIN from ActionsReactions</speak>"}}]}},"possibleIntents":[{"inputValueData":{},"intent":"actions.intent.TEXT"}]}],"userStorage":"{\"data\":{\"user_id\":\"add8fc49-f2d1-48ad-91e0-2dd4aaf2de2d\"}}"}}]}"""
        assertEquals(expected, response)
    }

    private fun JaicpWebhookConnector.processSync(request: HttpBotRequest) = json
        .parseJson(process(request)!!.output.toString("UTF-8"))
        .jsonObject["data"]
        .toString()

    private fun JaicpWebhookConnector.processAsync(request: HttpBotRequest) =
        process(request)!!.output.toString("UTF-8")
}
