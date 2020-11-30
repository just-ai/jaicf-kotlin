package com.justai.jaicf.channel.jaicp.logging


import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.jaicp.*
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpLogModel
import com.justai.jaicf.channel.jaicp.dto.jaicpNative
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.http.HttpClientFactory
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager.getOrCreateSessionId
import com.justai.jaicf.channel.jaicp.logging.internal.SessionData
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.logging.ConversationLogObfuscator
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.LoggingContext
import io.ktor.client.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext

/**
 * Implements [ConversationLogger] for JAICP Application Console.
 *
 * Any channel connected via [JaicpWebhookConnector] or [JaicpPollingConnector] will produce logs to JAICP.
 * If [JaicpConversationLogger] is added to array of loggers, but [BotRequest] is not received from JAICP, nothing will happen.
 *
 * @see [JaicpPollingConnector]
 * @see [JaicpWebhookConnector]
 * @see [JaicpBotRequest]
 * */
open class JaicpConversationLogger(
    accessToken: String,
    logObfuscators: List<ConversationLogObfuscator> = emptyList(),
    url: String = DEFAULT_PROXY_URL,
    logLevel: LogLevel = LogLevel.INFO,
    httpClient: HttpClient? = null
) : ConversationLogger(logObfuscators),
    WithLogger,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val client = httpClient ?: HttpClientFactory.create(logLevel)
    private val connector = ChatAdapterConnector(accessToken, url, client)

    override fun doLog(loggingContext: LoggingContext) {
        try {
            val req = loggingContext.request.jaicpNative?.jaicp ?: extractJaicpRequest(loggingContext) ?: return
            val session = getOrCreateSessionId(loggingContext.botContext, loggingContext.isNewSession)
            launch(MDCContext()) {
                doLogAsync(req, loggingContext, session)
            }
        } catch (e: Exception) {
            logger.debug("Failed to produce JAICP LogRequest: ", e)
        }
    }

    private suspend fun doLogAsync(req: JaicpBotRequest, ctx: LoggingContext, session: SessionData) =
        connector.processLogAsync(createLog(req, ctx, session))

    internal open fun createLog(req: JaicpBotRequest, ctx: LoggingContext, session: SessionData) =
        JaicpLogModel.fromRequest(req, ctx, session).also {
            logger.debug("Send log with sessionId: ${it.sessionId} isNewSession: ${it.isNewSession}")
        }

    private fun extractJaicpRequest(loggingContext: LoggingContext): JaicpBotRequest? {
        return try {
            loggingContext.httpBotRequest?.requestMetadata?.run {
                JSON.parse(JaicpBotRequest.serializer(), loggingContext.httpBotRequest?.requestMetadata!!)
            }
        } catch (e: Exception) {
            logger.debug("Logging skipped as loggingContext does not have valid JAICP Request")
            return null
        }
    }
}
