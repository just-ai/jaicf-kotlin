package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.agent.Handoff
import com.justai.jaicf.activator.llm.agent.HandoffException
import com.justai.jaicf.activator.llm.agent.handoffMessages
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
import java.util.concurrent.Executors
import kotlin.jvm.Throws
import kotlin.jvm.optionals.getOrNull

val DefaultOpenAIClient = OpenAIOkHttpClient.fromEnv()
private val toolsDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

class LLMActivatorAPI(
    val defaultProps: LLMProps = LLMProps(client = DefaultOpenAIClient),
) {
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
        origin: ActivatorContext? = null,
    ): LLMActivatorContext {
        val props = LLMProps.Builder(botContext, request).apply(props).build()
        val params = defaultProps.withOptions(props).toChatCompletionCreateParams().apply {
            if (botContext.handoffMessages.isEmpty()) {
                addUserMessage(request.input)
            }
        }.build()

        return LLMActivatorContext(this, params, props, botContext, request, origin)
    }

    internal fun callTools(context: LLMActivatorContext) = runBlocking(toolsDispatcher) {
        context.toolCalls.mapNotNull { call ->
            val function = call.function()
            context.props.tools?.find { t -> t.definition.simpleName == function.name() }?.let { tool ->
                async {
                    val args = function.arguments(tool.definition) as Any
                    val result = try {
                        @Suppress("UNCHECKED_CAST")
                        (tool.function as LLMToolFunction<Any>).invoke(context, args)
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
        context: LLMActivatorContext,
        results: List<LLMToolResult>,
    ): List<LLMToolResult> {
        val toolCallResults = results.toMutableList()
        if (toolCallResults.isEmpty()) {
            if (!context.hasToolCalls) {
                return emptyList()
            }
            toolCallResults.addAll(callTools(context))
        }

        var message = context.message
        message.toolCalls().ifPresent {
            message = context.message.toBuilder().toolCalls(
                context.message.toolCalls().get().filter {
                    it.function().name() != Handoff::class.java.simpleName
                }
            ).build()
        }

        val params = context.chatCompletionParams.toBuilder().apply {
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

        context.startStream(params)
        return toolCallResults
    }
}