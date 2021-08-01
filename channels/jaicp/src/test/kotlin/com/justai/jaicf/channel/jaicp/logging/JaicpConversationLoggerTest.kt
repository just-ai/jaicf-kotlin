package com.justai.jaicf.channel.jaicp.logging

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.JaicpBaseTest
import com.justai.jaicf.channel.jaicp.JaicpTestChannel
import com.justai.jaicf.channel.jaicp.ScenarioFactory
import com.justai.jaicf.channel.jaicp.asJaicpBotRequest
import com.justai.jaicf.channel.jaicp.channels.ChatApiChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.channel.jaicp.logging.internal.SessionData
import com.justai.jaicf.channel.jaicp.reactions.jaicp
import com.justai.jaicf.context.ExecutionContext
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class JaicpConversationLoggerTest : JaicpBaseTest() {
    private var actLog: JaicpLogModel by Delegates.notNull()
    private val expLog: JaicpLogModel by lazy {
        JSON.decodeFromString(JaicpLogModel.serializer(), getResourceAsString("logModel.json"))
    }
    private val conversationLogger = object : JaicpConversationLogger("", emptyList(), "") {
        override fun createLog(
            req: JaicpBotRequest,
            ctx: ExecutionContext,
            session: SessionData,
        ): JaicpLogModel = super.createLog(req, ctx, session).also {
            actLog = it
        }
    }
    private val echoBot = BotEngine(ScenarioFactory.echo(), conversationLoggers = arrayOf(conversationLogger))

    @Test
    fun `001 logging should set log model with session id for new user`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(requestFromResources.withClientId(testNumber))

        assertNotNull(actLog.sessionId)
        assertEquals(actLog.sessionId, actLog.response.responseData.sessionId)
        assertTrue(actLog.isNewUser)
        assertTrue(actLog.isNewSession)
    }

    @Test
    fun `002 logging should create correct log model`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(requestFromResources.withClientId(testNumber))

        println(JSON.encodeToString(actLog))
        println(JSON.encodeToString(expLog))

        assertEquals(
            expLog.withInvalidatedTime().withInvalidatedSessionId().withUserId(testNumber),
            actLog.withInvalidatedTime().withInvalidatedSessionId()
        )
    }

    @Test
    fun `003 logging should maintain sessionId between calls from one client`() {
        val channel = JaicpTestChannel(echoBot, ChatApiChannel)
        channel.process(requestFromResources.withQuery("Hello!").withClientId(testNumber))
        val sessionBefore = actLog.sessionId
        assertTrue(actLog.isNewSession)

        channel.process(requestFromResources.withQuery("test session id")
            .withClientId(testNumber))
        val sessionAfter = actLog.sessionId

        assertEquals(sessionBefore, sessionAfter)
        assertFalse(actLog.isNewSession)
    }

    @Test
    fun `004 logging should start new session`() {
        val scenario = Scenario {

            state("sid") {
                activators { regex("start session") }
                action {
                    reactions.jaicp?.startNewSession()
                }
            }

            fallback { reactions.say("You said: ${request.input}") }
        }
        val bot =
            BotEngine(scenario, conversationLoggers = arrayOf(conversationLogger), activators = arrayOf(RegexActivator))
        val channel = JaicpTestChannel(bot, ChatApiChannel)

        channel.process(requestFromResources.withQuery("Hello!").withClientId(testNumber))
        val sessionBefore = actLog.sessionId
        assertTrue(actLog.isNewSession)
        println(sessionBefore)

        channel.process(requestFromResources.withQuery("start session").withClientId(testNumber))
        val sessionAfter = actLog.sessionId
        println(sessionAfter)

        assertNotEquals(sessionBefore, sessionAfter)
        assertTrue(actLog.isNewSession)

        channel.process(requestFromResources.withQuery("Hello again!").withClientId(testNumber))

        assertNotEquals(sessionBefore, sessionAfter)
        assertFalse(actLog.isNewSession)
    }

    @Test
    fun `005 logging should end current session, next request should go to new session`() {
        val scenario = Scenario {

            state("sid") {
                activators { regex("end session") }
                action { reactions.jaicp?.endSession() }
            }

            fallback { reactions.say("You said: ${request.input}") }
        }
        val bot =
            BotEngine(scenario, conversationLoggers = arrayOf(conversationLogger), activators = arrayOf(RegexActivator))
        val channel = JaicpTestChannel(bot, ChatApiChannel)

        channel.process(requestFromResources.withQuery("Hello!").withClientId(testNumber))
        val sessionInitial = actLog.sessionId
        assertTrue(actLog.isNewSession)

        channel.process(requestFromResources.withQuery("end session").withClientId(testNumber))
        val sessionAfterEndReaction = actLog.sessionId

        assertFalse(actLog.isNewSession)
        assertEquals(sessionInitial, sessionAfterEndReaction)

        channel.process(requestFromResources.withQuery("Hello again!").withClientId(testNumber))
        val sessionAfterNewRequest = actLog.sessionId

        assertTrue(actLog.isNewSession)
        assertNotEquals(sessionInitial, sessionAfterNewRequest)
    }

    @Test
    fun `006 logging should create log model with channel data for telephony channel`() {
        JaicpTestChannel(echoBot, TelephonyChannel).process(requestFromResources.withClientId(testNumber))

        assertEquals(
            expLog.withInvalidatedTime().withInvalidatedSessionId().withUserId(testNumber),
            actLog.withInvalidatedTime().withInvalidatedSessionId()
        )
    }
}

private fun JaicpLogModel.withInvalidatedTime() = copy(timestamp = 0, processingTime = 0)

private fun JaicpLogModel.withInvalidatedSessionId(): JaicpLogModel {
    val responseData = response.responseData.copy(sessionId = "sessionId")
    return copy(sessionId = "sessionId", response = response.copy(responseData))
}

private fun JaicpLogModel.withUserId(uid: String) = copy(userId = uid)

private fun HttpBotRequest.withQuery(q: String) =
    receiveText().asJaicpBotRequest().copy(query = q).stringify().asHttpBotRequest()

private fun HttpBotRequest.withClientId(cid: String) =
    receiveText().asJaicpBotRequest().copy(channelUserId = cid).stringify().asHttpBotRequest()
