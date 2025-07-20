package com.justai.jaicf.activator.llm.action

import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMContext
import com.justai.jaicf.activator.llm.LLMInputs
import com.justai.jaicf.activator.llm.LLMMessage
import com.justai.jaicf.activator.llm.agent.handoffMessages
import com.justai.jaicf.activator.llm.build
import com.justai.jaicf.activator.llm.builder.build
import com.justai.jaicf.activator.llm.tool.*
import com.justai.jaicf.api.BotRequest
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
private val DefaultProps = _root_ide_package_.com.justai.jaicf.activator.llm.LLMProps(
    client = DefaultOpenAIClient,
    withUsages = true,
)

class LLMActionAPI(val defaultProps: com.justai.jaicf.activator.llm.LLMProps = DefaultProps) {
    fun createStreaming(
        params: ChatCompletionCreateParams,
        client: OpenAIClient? = null,
    ): StreamResponse<ChatCompletionChunk> {
        val client = client ?: defaultProps.client ?: DefaultOpenAIClient
        return client.chat().completions().createStreaming(params)
    }

    internal fun createContext(
        context: BotContext,
        request: BotRequest,
        props: com.justai.jaicf.activator.llm.LLMPropsBuilder = DefaultLLMProps,
    ): LLMContext {
        val props = defaultProps.withOptions(props.build(context, request))
        val params = props.toChatCompletionCreateParams().apply {
            if (context.handoffMessages.isEmpty()) {
                val builder = props.input ?: LLMInputs.TextOnly
                builder.invoke(request)?.forEach(::addMessage)
                    ?: throw IllegalArgumentException("Request is not supported: $request")
            } else {
                messages(props.messages.orEmpty()
                    .filter { it.isSystem() || it.isDeveloper() } +
                    context.handoffMessages.filter { !it.isSystem() && !it.isDeveloper() }
                )
                context.handoffMessages = emptyList()
            }
        }.build(props)

        return LLMContext(this, context, request, params, props)
    }

    @Throws(LLMToolInterruptionException::class)
    suspend fun LLMContext.callTool(call: ChatCompletionMessageToolCall): LLMToolResult {
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
                            context,
                            request,
                            this,
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

    suspend fun LLMContext.callTools(): List<Result<LLMToolResult>> {
        return coroutineScope {
            toolCalls().map { call ->
                async { runCatching { callTool(call) } }
            }.awaitAll()
        }
    }

    suspend fun LLMContext.submitToolResults(): List<LLMToolResult> {
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
        private lateinit var instance: LLMActionAPI

        fun init(defaultProps: com.justai.jaicf.activator.llm.LLMProps = DefaultProps): LLMActionAPI {
            if (::instance.isInitialized) {
                throw IllegalStateException("LLMActivatorAPI is initialized already")
            }
            instance = LLMActionAPI(defaultProps)
            return instance
        }

        val get: LLMActionAPI
            get() {
               if (!::instance.isInitialized) { init() }
               return instance
            }
    }
}
