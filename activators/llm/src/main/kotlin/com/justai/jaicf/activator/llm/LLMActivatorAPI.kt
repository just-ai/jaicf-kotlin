package com.justai.jaicf.activator.llm

import com.fasterxml.jackson.databind.DeserializationFeature
import com.justai.jaicf.activator.llm.agent.handoffMessages
import com.justai.jaicf.activator.llm.builder.build
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.JsonField
import com.openai.core.JsonNull
import com.openai.core.http.StreamResponse
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionCreateParams
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.Optional
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull


private val DefaultOpenAIClient = OpenAIOkHttpClient.fromEnv()
private val DefaultToolsExecutor = Executors.newCachedThreadPool()
private val DefaultProps = LLMProps(client = DefaultOpenAIClient)
private val DefaultHttpClient = HttpClient {
    install(SSE)
    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        }
    }
}

class LLMActivatorAPI(
    toolsExecutor: Executor = DefaultToolsExecutor,
    val defaultProps: LLMProps = DefaultProps,
    val httpClient: HttpClient = DefaultHttpClient,
) {
    private val toolsDispatcher = toolsExecutor.asCoroutineDispatcher()

    fun createStreaming(
        params: ChatCompletionCreateParams,
        client: OpenAIClient? = null,
    ): StreamResponse<ChatCompletionChunk> {
        val client = client ?: defaultProps.client ?: DefaultOpenAIClient
        return client.chat().completions().createStreaming(params)
    }

    internal fun createActivatorContext(
        botContext: BotContext,
        request: BotRequest,
        origin: ActivatorContext,
        props: LLMPropsBuilder = DefaultLLMProps,
    ): LLMActivatorContext {
        val props = defaultProps.withOptions(props.build(botContext, request))
        val params = props.toChatCompletionCreateParams().apply {
            if (botContext.handoffMessages.isEmpty()) {
                val builder = props.input ?: LLMInputs.TextOnly
                builder.invoke(request)?.forEach(::addMessage)
                    ?: throw IllegalArgumentException("Request is not supported: $request")
            } else {
                messages(props.messages.orEmpty()
                    .filter { it.isSystem() || it.isDeveloper() } +
                    botContext.handoffMessages.filter { !it.isSystem() && !it.isDeveloper() }
                )
                botContext.handoffMessages = emptyList()
            }
        }.build(props)

        return LLMActivatorContext(this, botContext, request, params, props, origin)
    }

    internal fun callTools(activator: LLMActivatorContext) = runBlocking(toolsDispatcher) {
        activator.toolCalls.map { call ->
            async {
                runCatching { activator.callTool(call) }
            }
        }.awaitAll()
    }

    internal fun submitToolResults(
        activator: LLMActivatorContext,
        results: List<LLMToolResult>,
    ): List<LLMToolResult> {
        val toolCallResults = results.toMutableList()
        val toolCallExceptions = mutableListOf<Throwable>()

        if (toolCallResults.isEmpty()) {
            if (!activator.hasToolCalls) {
                return emptyList()
            }
            val results = callTools(activator)
            toolCallResults.addAll(results.mapNotNull { it.getOrNull() })
            toolCallExceptions.addAll(results.mapNotNull { it.exceptionOrNull() })
        }

        var message = activator.message
        message.toolCalls().ifPresent {
            val toolCalls = message.toolCalls().get().filter { tc ->
                toolCallResults.any { it.callId == tc.id() }
            }
            message = message.toBuilder()
                .toolCalls(toolCalls.takeIf { it.isNotEmpty() }
                    ?.let { JsonField.of(toolCalls) }
                    ?: JsonNull())
                .build()
        }

        activator.params = activator.chatCompletionParams.toBuilder().apply {
            if (!toolCallResults.isEmpty()) {
                addMessage(message)
            }
            toolCallResults
                .map(LLMMessage::tool)
                .forEach(::addMessage)
        }.build()

        if (toolCallExceptions.isNotEmpty()) {
            throw toolCallExceptions.first()
        }

        activator.startStream()
        return toolCallResults
    }

    companion object {
        private lateinit var instance: LLMActivatorAPI

        fun init(
            toolsExecutor: Executor = DefaultToolsExecutor,
            defaultProps: LLMProps = DefaultProps,
        ): LLMActivatorAPI {
            if (::instance.isInitialized) {
                throw IllegalStateException("LLMActivatorAPI is initialized already")
            }
            instance = LLMActivatorAPI(toolsExecutor, defaultProps)
            return instance
        }

        val get: LLMActivatorAPI
            get() {
               if (!::instance.isInitialized) { init() }
               return instance
            }
    }
}
