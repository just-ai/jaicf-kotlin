package com.justai.jaicf.activator.llm

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.openai.core.JsonField
import com.openai.core.http.StreamResponse
import com.openai.helpers.ChatCompletionAccumulator
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionChunk.Choice.FinishReason
import com.openai.models.chat.completions.ChatCompletionCreateParams
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull


val ActivatorContext.llm
    get() = this as? LLMActivatorContext

val StructuredOutputMapper =
    JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(Jdk8Module())
        .addModule(JavaTimeModule())
        .build()

data class LLMActivatorContext(
    val api: LLMActivatorAPI,
    private var params: ChatCompletionCreateParams,
    private var response: StreamResponse<ChatCompletionChunk>,
    val props: LLMProps,
    val context: BotContext,
    val request: BotRequest,
    val origin: ActivatorContext? = null,
) : EventActivatorContext(LLMEvent.RESPONSE) {
    private lateinit var acc: ChatCompletionAccumulator
    private lateinit var stream: Stream<ChatCompletionChunk>

    init {
        setResponse(params, response)
    }

    internal fun setResponse(
        params: ChatCompletionCreateParams,
        response: StreamResponse<ChatCompletionChunk>,
    ) {
        this.params = params
        this.response = response
        this.acc = ChatCompletionAccumulator.create()
        this.stream = response.stream()
            .peek(acc::accumulate)
            .peek(::processFinalChunk)
    }

    private fun processFinalChunk(chunk: ChatCompletionChunk) {
        chunk.choices().first().finishReason().ifPresent { reason ->
            response.close()
            if (reason == FinishReason.STOP || reason == FinishReason.LENGTH) {
                props.messages?.ifLLMMemory { memory ->
                    val toolCalls = message.toolCalls()
                        .getOrDefault(emptyList()).takeIf { it.isNotEmpty() }
                    memory.set(
                        // fix nullable tool calls in assistant message
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

    fun stream() = stream

    val chatCompletionParams
        get() = params

    val chatCompletion: ChatCompletion
        get() {
            return try {
                acc.chatCompletion()
            } catch (e: Exception) {
                stream.count()
                acc.chatCompletion()
            }
        }
}

val LLMActivatorContext.choice
    get() = chatCompletion.choices().first()

val LLMActivatorContext.message
    get() = choice.message()

val LLMActivatorContext.content
    get() = message.content().getOrNull()

val LLMActivatorContext.toolCalls
    get() = message.toolCalls().getOrDefault(emptyList())

val LLMActivatorContext.hasToolCalls
    get() = toolCalls.isNotEmpty()

val LLMActivatorContext.deltaStream: Stream<ChatCompletionChunk.Choice.Delta>
    get() = stream()
        .map { it.choices().first().delta() }

val LLMActivatorContext.contentStream: Stream<String>
    get() = deltaStream
        .map { it.content() }
        .filter { it.isPresent }
        .map { it.get() }

fun LLMActivatorContext.callTools(): List<LLMToolResult> {
    if (!hasToolCalls || props.tools.isNullOrEmpty()) {
        return emptyList()
    }
    return api.callTools(this)
}

fun LLMActivatorContext.submitToolResults(results: List<LLMToolResult> = emptyList()): List<LLMToolResult> {
    return api.submitToolResults(this, results)
}

fun LLMActivatorContext.withToolCalls(
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

fun LLMActivatorContext.awaitFinalMessage() =
    withToolCalls().message

fun LLMActivatorContext.awaitFinalContent() =
    awaitFinalMessage().content().getOrNull()

inline fun <reified T> LLMActivatorContext.awaitStructuredContent(): T? =
    awaitFinalContent().let { StructuredOutputMapper.readValue(it, T::class.java) }