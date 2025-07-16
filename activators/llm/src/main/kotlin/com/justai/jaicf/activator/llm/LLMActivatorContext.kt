package com.justai.jaicf.activator.llm

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.StrictActivatorContext
import com.openai.core.JsonField
import com.openai.core.http.StreamResponse
import com.openai.helpers.ChatCompletionAccumulator
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionChunk.Choice.FinishReason
import com.openai.models.chat.completions.ChatCompletionCreateParams
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.stream.Stream
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull


typealias LLMWithToolCalls = suspend LLMActivatorContext.(results: List<LLMToolResult>) -> Unit

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
    internal var params: ChatCompletionCreateParams,
    val props: LLMProps,
    val origin: ActivatorContext,
) : StrictActivatorContext() {
    private lateinit var response: StreamResponse<ChatCompletionChunk>
    private lateinit var stream: Stream<ChatCompletionChunk>
    private lateinit var acc: ChatCompletionAccumulator

    private suspend fun throwIfCancelled() {
        try {
            yield()
        } catch (e: CancellationException) {
            closeStream()
            throw e
        }
    }

    private suspend fun <T> withActiveJob(block: suspend () -> T): T {
        throwIfCancelled()
        return block.invoke()
    }

    internal suspend fun startStream() = withActiveJob {
        acc = ChatCompletionAccumulator.create()
        response = api.createStreaming(params)
        stream = response.stream()
            .peek(acc::accumulate)
            .peek(::processFinalChunk)
        throwIfCancelled()
    }

    private fun processFinalChunk(chunk: ChatCompletionChunk) {
        val reason = chunk.choices().first().finishReason().getOrNull()
        if (reason != null) {
            response.close()
            runBlocking {
                val message = message()
                val toolCalls = toolCalls()
                if (reason == FinishReason.STOP || reason == FinishReason.LENGTH) {
                    props.messages?.ifLLMMemory { memory ->
                        memory.set(
                            // fix nullable tool calls in an assistant message
                            params.toBuilder()
                                .addMessage(message.toBuilder()
                                    .toolCalls(JsonField.ofNullable(toolCalls.takeIf { it.isNotEmpty() }))
                                    .build()
                                ).build().messages()
                        )
                    }
                }
            }
        }
    }

    val chatCompletionParams
        get() = params

    fun closeStream() {
        if (::response.isInitialized) {
            response.close()
        }
    }

    suspend fun getStream(): Stream<ChatCompletionChunk> = withActiveJob {
        if (!::stream.isInitialized) {
            startStream()
        }
        val channel = Channel<ChatCompletionChunk>(capacity = Channel.UNLIMITED)
        val job = CoroutineScope(coroutineContext).launch {
            try {
                val iterator = stream.iterator()
                while (iterator.hasNext()) {
                    val chunk = iterator.next()
                    channel.send(chunk)
                    yield()
                }
            } finally {
                channel.close()
                closeStream()
            }
        }
        Stream.generate {
            runBlocking {
                channel.receiveCatching().getOrNull()
            }
        }
            .takeWhile { it != null }
            .map { it!! }
            .onClose {
                job.cancel()
            }
    }

    suspend fun awaitChatCompletion(): ChatCompletion {
        return try {
            acc.chatCompletion()
        } catch (e: Exception) {
            getStream().count()
            acc.chatCompletion()
        }
    }

    suspend fun firstChoice() = awaitChatCompletion().choices().first()

    suspend fun message() = firstChoice().message()

    suspend fun content() = message().content().getOrNull()

    suspend fun toolCalls() = message().toolCalls().getOrDefault(emptyList())

    suspend fun hasToolCalls() = toolCalls().isNotEmpty()

    suspend fun deltaStream() = getStream().map { it.choices().first().delta() }

    suspend fun contentStream() = deltaStream()
        .map { it.content() }
        .filter { it.isPresent }
        .map { it.get() }

    suspend fun withToolCalls(
        block: LLMWithToolCalls? = null
    ) = apply {
        var toolCalls = false
        var results: List<LLMToolResult> = emptyList()
        do {
            results = withActiveJob {
                block?.invoke(this, results)
                toolCalls = hasToolCalls()
                api.run { submitToolResults() }
            }
        } while (toolCalls)
    }

    suspend fun awaitFinalMessage() =
        withToolCalls().message()

    suspend fun awaitFinalContent() =
        awaitFinalMessage().content().getOrNull()

    suspend inline fun <reified T> awaitStructuredContent(): T? =
        awaitFinalContent().let { StructuredOutputMapper.readValue(it, T::class.java) }
}