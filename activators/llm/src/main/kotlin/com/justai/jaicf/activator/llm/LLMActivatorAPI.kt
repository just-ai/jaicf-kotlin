package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.agent.Handoff
import com.justai.jaicf.activator.llm.agent.HandoffException
import com.justai.jaicf.activator.llm.agent.handoffMessages
import com.justai.jaicf.activator.llm.tool.LLMToolCall
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.LLMToolFunction
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.http.StreamResponse
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionCreateParams
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.jvm.Throws
import kotlin.jvm.optionals.getOrNull


class LLMActivatorAPI(
    toolsExecutor: Executor = DefaultToolsExecutor,
    val defaultProps: LLMProps = DefaultProps,
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
        props: LLMPropsBuilder = DefaultLLMProps,
        botContext: BotContext,
        request: BotRequest,
        origin: ActivatorContext,
    ): LLMActivatorContext {
        val props = props.build(botContext, request)
        val params = defaultProps.withOptions(props).toChatCompletionCreateParams().apply {
            if (botContext.handoffMessages.isEmpty()) {
                val builder = props.input ?: LLMInputs.TextOnly
                builder.invoke(request)?.forEach(::addMessage)
                    ?: throw IllegalArgumentException("Request is not supported: $request")
            }
        }.build()

        return LLMActivatorContext(this, botContext, request, params, props, origin)
    }

    internal fun callTools(activator: LLMActivatorContext) = runBlocking(toolsDispatcher) {
        val context = LLMToolCallContext(activator.botContext, activator.request)
        activator.toolCalls.mapNotNull { call ->
            val function = call.function()
            activator.props.tools?.find { t -> t.definition.name == function.name() }?.let { tool ->
                async {
                    val args = function.arguments(tool.definition.parametersType) as Any
                    val result = try {
                        @Suppress("UNCHECKED_CAST")
                        (tool.function as LLMToolFunction<Any>)
                            .invoke(context, LLMToolCall(function.name(), call.id(), args))
                    } catch (e: Exception) {
                        "Error: ${e.message}"
                    }
                    LLMToolResult(
                        callId = call.id(),
                        name = function.name(),
                        arguments = args,
                        result = result,
                    )
                }
            }
        }.awaitAll()
    }

    @Throws(HandoffException::class)
    internal fun submitToolResults(
        activator: LLMActivatorContext,
        results: List<LLMToolResult>,
    ): List<LLMToolResult> {
        val toolCallResults = results.toMutableList()
        if (toolCallResults.isEmpty()) {
            if (!activator.hasToolCalls) {
                return emptyList()
            }
            toolCallResults.addAll(callTools(activator))
        }

        var message = activator.message
        message.toolCalls().ifPresent {
            message = activator.message.toBuilder().toolCalls(
                activator.message.toolCalls().get().filter {
                    it.function().name() != Handoff::class.java.simpleName
                }
            ).build()
        }

        val params = activator.chatCompletionParams.toBuilder().apply {
            if (message.content().getOrNull()?.isNotEmpty() == true || message.toolCalls().getOrNull()?.isNotEmpty() == true) {
                addMessage(message)
            }
            toolCallResults
                .filter { it.arguments !is Handoff }
                .map(LLMMessage::tool)
                .forEach(::addMessage)
        }.build()

        toolCallResults.find { it.arguments is Handoff }?.also {
            throw HandoffException(
                it.arguments<Handoff>().agentName,
                params.messages().filter { msg ->!msg.isSystem() && !msg.isDeveloper() },
            )
        }

        activator.startStream(params)
        return toolCallResults
    }

    companion object {
        private val DefaultOpenAIClient = OpenAIOkHttpClient.fromEnv()
        private val DefaultToolsExecutor = Executors.newCachedThreadPool()
        private val DefaultProps = LLMProps(client = DefaultOpenAIClient)

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
