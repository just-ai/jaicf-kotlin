package com.justai.jaicf.channel.jaicp.logging

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.channels.ChatApiChannel
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.channel.jaicp.reactions.chatapi
import com.justai.jaicf.logging.LoggingContext
import com.justai.jaicf.model.scenario.Scenario
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class JaicpConversationLoggerTest : JaicpBaseTest() {
    private var actLog: JaicpLogModel by Delegates.notNull()
    private val expLog: JaicpLogModel by lazy {
        JSON.parse(JaicpLogModel.serializer(), getResourceAsString("logModel.json"))
    }
    private val conversationLogger = object : JaicpConversationLogger("") {
        override fun createLog(
            req: JaicpBotRequest,
            ctx: LoggingContext,
            session: JaicpConversationSessionData
        ): JaicpLogModel = super.createLog(req, ctx, session).also { actLog = it }
    }
    private val spyLogger = spyk(conversationLogger)
    private val echoBot = BotEngine(ScenarioFactory.echo().model, conversationLoggers = arrayOf(spyLogger))

    @Test
    fun `001 logging should set log model with session id for new user`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(request.withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }

        assertNotNull(actLog.sessionId)
        assertTrue(actLog.isNewSession)
    }

    @Test
    fun `002 logging should create correct log model`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(request.withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }

        assertEquals(
            expLog.withInvalidatedTime().withInvalidatedSessionId().withUserId(testNumber),
            actLog.withInvalidatedTime().withInvalidatedSessionId()
        )
    }

    @Test
    fun `003 logging should maintain sessionId between calls from one client`() {
        JaicpTestChannel(echoBot, ChatApiChannel).process(request.withQuery("Hello!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionBefore = actLog.sessionId

        JaicpTestChannel(echoBot, ChatApiChannel).process(request.withQuery("test session id").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionAfter = actLog.sessionId

        assertEquals(sessionBefore, sessionAfter)
    }

    @Test
    fun `004 logging should start new session`() {
        val scenario = object : Scenario() {
            init {
                fallback { reactions.say("You said: ${request.input}") }
                state("sid") {
                    activators { regex("test session id") }
                    action {
                        reactions.chatapi?.startNewSession()
                    }
                }
            }
        }
        val bot =
            BotEngine(scenario.model, conversationLoggers = arrayOf(spyLogger), activators = arrayOf(RegexActivator))

        JaicpTestChannel(bot, ChatApiChannel).process(request.withQuery("Hello!").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionBefore = actLog.sessionId

        JaicpTestChannel(bot, ChatApiChannel).process(request.withQuery("test session id").withClientId(testNumber))
        verify(timeout = 500) { spyLogger.createLog(any(), any(), any()) }
        val sessionAfter = actLog.sessionId

        assertNotEquals(sessionBefore, sessionAfter)
    }
}

private fun JaicpLogModel.withInvalidatedTime() = copy(timestamp = 0, processingTime = 0)
private fun JaicpLogModel.withInvalidatedSessionId() = copy(sessionId = "sessionId")
private fun JaicpLogModel.withUserId(uid: String) = copy(userId = uid)

private fun HttpBotRequest.withQuery(q: String) =
    receiveText().asJaicpBotRequest().copy(query = q).stringify().asHttpBotRequest()

private fun HttpBotRequest.withClientId(cid: String) =
    receiveText().asJaicpBotRequest().copy(channelUserId = cid).stringify().asHttpBotRequest()
