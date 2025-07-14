package com.justai.jaicf.activator.llm

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolCall
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.LLMToolFunction
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.openai.core.JsonField
import com.openai.core.http.StreamResponse
import com.openai.helpers.ChatCompletionAccumulator
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionChunk.Choice.FinishReason
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull


val StructuredOutputMapper =
    JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(Jdk8Module())
        .addModule(JavaTimeModule())
        .build()

data class LLMActivatorContext(
    val api: LLMActivatorAPI,
    internal val botContext: BotContext,
    internal val request: BotRequest,
    private var params: ChatCompletionCreateParams,
    val props: LLMProps,
    val origin: ActivatorContext,
) : StrictActivatorContext() {
    private lateinit var stream: Stream<ChatCompletionChunk>
    private lateinit var acc: ChatCompletionAccumulator

    internal fun startStream(params: ChatCompletionCreateParams) {
        this.params = params
        acc = ChatCompletionAccumulator.create()
        stream = api.createStreaming(params).let { response ->
            response.stream()
                .peek(acc::accumulate)
                .peek({ c -> response.processFinalChunk(c) })
        }
    }

    private fun StreamResponse<ChatCompletionChunk>.processFinalChunk(chunk: ChatCompletionChunk) {
        chunk.choices().first().finishReason().ifPresent { reason ->
            close()
            if (reason == FinishReason.STOP || reason == FinishReason.LENGTH) {
                props.messages?.ifLLMMemory { memory ->
                    val toolCalls = message.toolCalls()
                        .getOrDefault(emptyList()).takeIf { it.isNotEmpty() }
                    memory.set(
                        // fix nullable tool calls in an assistant message
                        params.toBuilder()
                            .addMessage(message.toBuilder()
                                .toolCalls(JsonField.ofNullable(toolCalls))
                                .build()
                            ).build().messages()
                    )
                }
            }
        }
    }

    fun getStream(): Stream<ChatCompletionChunk> {
        if (!::stream.isInitialized) {
            startStream(params)
        }
        return stream
    }

    val chatCompletionParams
        get() = params

    val chatCompletion: ChatCompletion
        get() {
            return try {
                acc.chatCompletion()
            } catch (e: Exception) {
                getStream().count()
                acc.chatCompletion()
            }
        }

    val choice
        get() = chatCompletion.choices().first()

    val message
        get() = choice.message()

    val content
        get() = message.content().getOrNull()

    val toolCalls
        get() = message.toolCalls().getOrDefault(emptyList())

    val hasToolCalls
        get() = toolCalls.isNotEmpty()

    val deltaStream: Stream<ChatCompletionChunk.Choice.Delta>
        get() = getStream()
            .map { it.choices().first().delta() }

    val contentStream: Stream<String>
        get() = deltaStream
            .map { it.content() }
            .filter { it.isPresent }
            .map { it.get() }

    fun callTool(call: ChatCompletionMessageToolCall): LLMToolResult {
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
                    "Error: ${e.message}"
                }
            } ?: "Error: no tool found with name ${function.name()}"
        )
    }

    fun callTools() = hasToolCalls.ifTrue {
        api.callTools(this)
    } ?: emptyList()

    fun submitToolResults(results: List<LLMToolResult> = emptyList()) =
        api.submitToolResults(this, results)

    fun withToolCalls(
        block: (LLMActivatorContext.(results: List<LLMToolResult>) -> Unit)? = null
    ) = apply {
        var toolCalls: Boolean
        var results: List<LLMToolResult> = emptyList()
        do {
            block?.invoke(this, results)
            toolCalls = hasToolCalls
            results = submitToolResults()
        } while (toolCalls)
    }

    fun awaitFinalMessage() =
        withToolCalls().message

    fun awaitFinalContent() =
        awaitFinalMessage().content().getOrNull()

    inline fun <reified T> awaitStructuredContent(): T? =
        awaitFinalContent().let { StructuredOutputMapper.readValue(it, T::class.java) }
}