package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.agent.handoffMessages
import com.justai.jaicf.activator.llm.builder.build
import com.justai.jaicf.activator.llm.tool.LLMToolCall
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.LLMToolFunction
import com.justai.jaicf.activator.llm.tool.LLMToolInterruptionException
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
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


private val DefaultOpenAIClient = OpenAIOkHttpClient.fromEnv()
private val DefaultProps = LLMProps(client = DefaultOpenAIClient)

class LLMActivatorAPI(val defaultProps: LLMProps = DefaultProps) {
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

    @Throws(LLMToolInterruptionException::class)
    suspend fun LLMActivatorContext.callTool(call: ChatCompletionMessageToolCall): LLMToolResult {
        val function = call.function()
        val tool = props.tools?.find { t -> t.definition.name == function.name() }
        val args = tool?.arguments(call)

        return LLMToolResult(
            callId = call.id(),
            name = function.name(),
            arguments = args,
            result = tool?.let { tool ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    (tool.function as LLMToolFunction<Any>).invoke(
                        LLMToolCallContext(
                            this,
                            botContext,
                            request,
                            LLMToolCall(function.name(), call.id(), args!!, call)
                        )
                    )
                } catch (e: Exception) {
                    if (e is LLMToolInterruptionException) throw e
                    if (e is CancellationException) throw e
                    "Error: ${e.message}"
                }
            } ?: "Error: no tool found with name ${function.name()}"
        )
    }

    suspend fun LLMActivatorContext.callTools(): List<Result<LLMToolResult>> {
        return coroutineScope {
            toolCalls().map { call ->
                async { runCatching { callTool(call) } }
            }.awaitAll()
        }
    }

    suspend fun LLMActivatorContext.submitToolResults(): List<LLMToolResult> {
        if (!hasToolCalls()) {
            return emptyList()
        }

        val results = callTools()
        val toolCallResults = results.mapNotNull { it.getOrNull() }
        val toolCallExceptions = results.mapNotNull { it.exceptionOrNull() }

        var message = message()
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

        params = chatCompletionParams.toBuilder().apply {
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

        startStream()
        return toolCallResults
    }

    companion object {
        private lateinit var instance: LLMActivatorAPI

        fun init(defaultProps: LLMProps = DefaultProps): LLMActivatorAPI {
            if (::instance.isInitialized) {
                throw IllegalStateException("LLMActivatorAPI is initialized already")
            }
            instance = LLMActivatorAPI(defaultProps)
            return instance
        }

        val get: LLMActivatorAPI
            get() {
               if (!::instance.isInitialized) { init() }
               return instance
            }
    }
}
