package com.justai.jaicf.channel.jaicp.logging

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.channels.ChatApiChannel
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.channel.jaicp.logging.internal.SessionData
import com.justai.jaicf.channel.jaicp.reactions.chatapi
import com.justai.jaicf.context.ExecutionContext
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates
import kotlin.test.*

internal class JaicpConversationLoggerTest : JaicpBaseTest() {
    private var actLog: JaicpLogModel by Delegates.notNull()
    private val expLog: JaicpLogModel by lazy {
        JSON.decodeFromString(JaicpLogModel.serializer(), getResourceAsString("logModel.json"))
    }
    private val conversationLogger = object : JaicpConversationLogger("", emptyList(), "") {
        override fun createLog(
            req: JaicpBotRequest,
            ctx: ExecutionContext,
            session: SessionData
        ): JaicpLogModel = super.createLog(req, ctx, session).also { actLog = it }
    }
    private val spyLogger = spyk(conversationLogger)
    private val echoBot = BotEngine(ScenarioFactory.echo(), conversationLoggers = arrayOf(spyLogger))

    @Test
    fun `001 logging should set log model with session id for new user`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(requestFromResources.withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }

        assertNotNull(actLog.sessionId)
        assertTrue(actLog.isNewSession)
    }

    @Test
    fun `002 logging should create correct log model`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(requestFromResources.withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }

        assertEquals(
            expLog.withInvalidatedTime().withInvalidatedSessionId().withUserId(testNumber),
            actLog.withInvalidatedTime().withInvalidatedSessionId()
        )
    }

    @Test
    fun `003 logging should maintain sessionId between calls from one client`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(requestFromResources.withQuery("Hello!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionBefore = actLog.sessionId
        assertTrue(actLog.isNewSession)

        JaicpTestChannel(echoBot, ChatApiChannel).process(requestFromResources.withQuery("test session id").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionAfter = actLog.sessionId

        assertEquals(sessionBefore, sessionAfter)
        assertFalse(actLog.isNewSession)
    }

    @Test
    fun `004 logging should start new session`() {
        val scenario = Scenario {

            state("sid") {
                activators { regex("start session") }
                action { reactions.chatapi?.startNewSession() }
            }

            fallback { reactions.say("You said: ${request.input}") }
        }
        val bot =
            BotEngine(scenario, conversationLoggers = arrayOf(spyLogger), activators = arrayOf(RegexActivator))
        val channel = JaicpTestChannel(bot, ChatApiChannel)

        channel.process(requestFromResources.withQuery("Hello!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionBefore = actLog.sessionId
        assertTrue(actLog.isNewSession)

        channel.process(requestFromResources.withQuery("start session").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionAfter = actLog.sessionId

        assertNotEquals(sessionBefore, sessionAfter)
        assertTrue(actLog.isNewSession)

        channel.process(requestFromResources.withQuery("Hello again!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }

        assertNotEquals(sessionBefore, sessionAfter)
        assertFalse(actLog.isNewSession)
    }

    @Test
    fun `005 logging should end current session, next request should go to new session`() {
        val scenario = Scenario {

            state("sid") {
                activators { regex("end session") }
                action { reactions.chatapi?.endSession() }
            }

            fallback { reactions.say("You said: ${request.input}") }
        }
        val bot =
            BotEngine(scenario, conversationLoggers = arrayOf(spyLogger), activators = arrayOf(RegexActivator))
        val channel = JaicpTestChannel(bot, ChatApiChannel)

        channel.process(requestFromResources.withQuery("Hello!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionInitial = actLog.sessionId
        assertTrue(actLog.isNewSession)

        channel.process(requestFromResources.withQuery("end session").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionAfterEndReaction = actLog.sessionId

        assertFalse(actLog.isNewSession)
        assertEquals(sessionInitial, sessionAfterEndReaction)

        channel.process(requestFromResources.withQuery("Hello again!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionAfterNewRequest = actLog.sessionId

        assertTrue(actLog.isNewSession)
        assertNotEquals(sessionInitial, sessionAfterNewRequest)
    }

    @Test
    fun `006 logging should create log model with channel data for telephony channel`() {
        JaicpTestChannel(echoBot, TelephonyChannel).process(requestFromResources.withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }

        assertEquals(
            expLog.withInvalidatedTime().withInvalidatedSessionId().withUserId(testNumber),
            actLog.withInvalidatedTime().withInvalidatedSessionId()
        )
    }
}

private fun JaicpLogModel.withInvalidatedTime() = copy(timestamp = 0, processingTime = 0)

private fun JaicpLogModel.withInvalidatedSessionId() = copy(sessionId = "sessionId")

private fun JaicpLogModel.withUserId(uid: String) = copy(userId = uid)

private fun HttpBotRequest.withQuery(q: String) =
    receiveText().asJaicpBotRequest().copy(query = q).stringify().asHttpBotRequest()

private fun HttpBotRequest.withClientId(cid: String) =
    receiveText().asJaicpBotRequest().copy(channelUserId = cid).stringify().asHttpBotRequest()
