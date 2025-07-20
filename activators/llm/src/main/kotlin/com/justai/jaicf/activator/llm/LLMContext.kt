package com.justai.jaicf.activator.llm

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.justai.jaicf.activator.llm.action.LLMActionAPI
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.core.JsonField
import com.openai.core.http.StreamResponse
import com.openai.helpers.ChatCompletionAccumulator
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.completions.CompletionUsage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.stream.Stream
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

typealias LLMWithToolCalls = suspend LLMContext.(results: List<LLMToolResult>) -> Unit

class LLMContext(
    val api: LLMActionAPI,
    internal val context: BotContext,
    internal val request: BotRequest,
    internal var params: ChatCompletionCreateParams,
    val props: LLMProps,
) {
    private lateinit var response: StreamResponse<ChatCompletionChunk>
    private lateinit var stream: Stream<ChatCompletionChunk>
    private lateinit var acc: ChatCompletionAccumulator

    private var completed: CompletableDeferred<List<ChatCompletionChunk>>? = null

    val chatCompletionParams
        get() = params

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
        completed = null
        acc = ChatCompletionAccumulator.Companion.create()
        response = api.createStreaming(params)
        stream = response.stream()
        throwIfCancelled()
    }

    fun closeStream() {
        if (::response.isInitialized) {
            response.close()
        }
    }

    suspend fun chunkStream(): Stream<ChatCompletionChunk> = withActiveJob {
        if (!::stream.isInitialized) {
            startStream()
        }
        var start = false
        synchronized(this) {
            if (completed == null) {
                start = true
                completed = CompletableDeferred()
            }
        }

        if (!start) {
            completed!!.await().stream()
        } else {
            channelStream { channel ->
                var finished = false
                val chunks = mutableListOf<ChatCompletionChunk>()
                try {
                    val iterator = stream.iterator()
                    while (iterator.hasNext()) {
                        val chunk = iterator.next()
                        yield()

                        chunks.add(chunk)
                        channel.send(chunk)

                        val reason = chunk.choices().first().finishReason().getOrNull()
                        if (reason != null) {
                            finished = true
                            acc.accumulate(chunk)

                            val message = message()
                            val toolCalls = toolCalls()
                            if (reason == ChatCompletionChunk.Choice.FinishReason.Companion.STOP || reason == ChatCompletionChunk.Choice.FinishReason.Companion.LENGTH) {
                                props.messages?.ifLLMMemory { memory ->
                                    memory.set(
                                        // fix nullable tool calls in an assistant message
                                        params.toBuilder()
                                            .addMessage(message.toBuilder()
                                                .toolCalls(JsonField.Companion.ofNullable(toolCalls.takeIf { it.isNotEmpty() }))
                                                .build()
                                            ).build().messages()
                                    )
                                }
                            }
                        } else if (finished && chunk.usage().isPresent) {
                            acc.accumulate(chunk)
                            break
                        } else if (!finished) {
                            acc.accumulate(chunk)
                        }
                    }
                } finally {
                    completed!!.complete(chunks)
                    closeStream()
                }
            }
        }
    }

    suspend fun chatCompletion(): ChatCompletion {
        return try {
            acc.chatCompletion()
        } catch (e: Exception) {
            chunkStream().count()
            acc.chatCompletion()
        }
    }

    suspend fun choice() = chatCompletion().choices().first()

    suspend fun message() = choice().message()

    suspend fun content() = message().content().getOrNull()

    suspend fun toolCalls() = message().toolCalls().getOrDefault(emptyList())

    suspend fun hasToolCalls() = toolCalls().isNotEmpty()

    suspend fun callAndSubmitTools() = api.run { submitToolResults() }

    suspend fun deltaStream() = chunkStream().map { it.choices().first().delta() }

    suspend fun contentStream() = deltaStream()
        .map { it.content() }
        .filter { it.isPresent }
        .map { it.get() }

    suspend fun eventStream(callTools: Boolean = true): Stream<LLMEvent> = channelStream { channel ->
        suspend fun processChunks() {
            channel.send(LLMEvent.Start())
            var reason: ChatCompletionChunk.Choice.FinishReason? = null
            var usage: CompletionUsage? = null
            for (chunk in chunkStream()) {
                val choice = chunk.choices().first()
                reason = reason ?: choice.finishReason().getOrNull()
                usage = usage ?: chunk.usage().getOrNull()
                val delta = choice.delta()
                channel.send(LLMEvent.Chunk(chunk))
                channel.send(LLMEvent.Delta(delta))
                if (delta.content().isPresent) {
                    channel.send(LLMEvent.ContentDelta(delta.content().get()))
                }
            }
            val message = message()
            if (message.content().isPresent) {
                channel.send(LLMEvent.Content(message.content().get()))
            }
            if (message.toolCalls().isPresent) {
                val toolCalls = message.toolCalls().get()
                if (toolCalls.isNotEmpty()) {
                    channel.send(LLMEvent.ToolCalls(toolCalls))
                }
            }
            channel.send(LLMEvent.Message(message))
            if (reason != null) {
                channel.send(LLMEvent.Finish(reason.value(), usage))
            }
        }

        if (callTools) {
            withToolCalls { results ->
                if (results.isNotEmpty()) {
                    channel.send(LLMEvent.ToolCallResults(results))
                }
                processChunks()
            }
        } else {
            processChunks()
        }
    }

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

val StructuredOutputMapper =
    JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(Jdk8Module())
        .addModule(JavaTimeModule())
        .build()

private suspend inline fun <T> channelStream(
    crossinline block: suspend (Channel<T>) -> Unit
): Stream<T> {
    val channel = Channel<T>(Channel.UNLIMITED)
    val job = CoroutineScope(coroutineContext).launch {
        try {
            block(channel)
        } finally {
            channel.close()
        }
    }
    return Stream.generate {
        runBlocking { channel.receiveCatching().getOrNull() }
    }
        .takeWhile { it != null }
        .map { it!! }
        .onClose { job.cancel() }
}