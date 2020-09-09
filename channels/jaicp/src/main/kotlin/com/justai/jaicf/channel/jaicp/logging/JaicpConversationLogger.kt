package com.justai.jaicf.channel.jaicp.logging


import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.jaicp.DEFAULT_PROXY_URL
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.LogModel
import com.justai.jaicf.channel.jaicp.dto.native
import com.justai.jaicf.channel.jaicp.http.ChatAdapterConnector
import com.justai.jaicf.channel.jaicp.http.HttpClientFactory
import com.justai.jaicf.channel.jaicp.JaicpPollingConnector
import com.justai.jaicf.channel.jaicp.JaicpWebhookConnector
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.LoggingContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.logging.ConversationLogObfuscator
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.extractObfuscatedInput
import com.justai.jaicf.logging.getObfuscatedReactions
import io.ktor.client.*
import io.ktor.client.features.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
class JaicpConversationLogger(
    accessToken: String,
    override val logObfuscator: ConversationLogObfuscator? = null,
    url: String = DEFAULT_PROXY_URL,
    logLevel: LogLevel = LogLevel.INFO,
    httpClient: HttpClient? = null
) : ConversationLogger,
    WithLogger,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val client = httpClient ?: HttpClientFactory.withLogLevel(logLevel)
    private val connector = ChatAdapterConnector(accessToken, url, client)

    override fun produce(
        activationContext: ActivationContext?,
        botContext: BotContext,
        request: BotRequest,
        loggingContext: LoggingContext
    ) {
        try {
            launch {
                produceInternal(request, botContext, loggingContext, activationContext)
            }
        } catch (e: Exception) {
            logger.debug("Failed to produce JAICP LogRequest: ", e)
        }
    }

    private suspend fun produceInternal(
        request: BotRequest,
        botContext: BotContext,
        loggingContext: LoggingContext,
        activationContext: ActivationContext?
    ) {
        val jaicpBotRequest = request.native?.jaicp ?: extractJaicpRequest(loggingContext) ?: return
        val input = extractObfuscatedInput(activationContext, botContext, request, loggingContext)
        val reactions = getObfuscatedReactions(activationContext, botContext, request, loggingContext)
        val logModel = LogModel.fromRequest(jaicpBotRequest, reactions, activationContext, input)

        connector.processLogAsync(logModel)
    }

    private fun extractJaicpRequest(loggingContext: LoggingContext): JaicpBotRequest? {
        return try {
            loggingContext.httpBotRequest?.jaicpRawRequest?.run {
                JSON.parse(JaicpBotRequest.serializer(), loggingContext.httpBotRequest?.jaicpRawRequest!!)
            }
        } catch (e: Exception) {
            return null
        }.also {
            if (it == null) logger.debug("Logging skipped as loggingContext does not have valid JAICP Request")
        }
    }
}
