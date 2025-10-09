package com.justai.jaicf.activator.llm.action

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.*
import com.justai.jaicf.activator.llm.tool.*
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.stream
import com.openai.core.JsonField
import com.openai.core.JsonNull
import com.openai.core.http.StreamResponse
import com.openai.helpers.ChatCompletionAccumulator
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.completions.CompletionUsage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.stream.Stream
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull


typealias LLMWithToolCalls = suspend LLMContext.(results: List<LLMToolResult>) -> Unit

data class LLMActionContext<A: ActivatorContext, B: BotRequest, R: Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: B,
    override val reactions: R,
    val llm: LLMContext,
) : ActionContext<A, B, R>(context, activator, request, reactions) {
    private lateinit var response: StreamResponse<ChatCompletionChunk>
    private lateinit var stream: Stream<ChatCompletionChunk>
    private lateinit var acc: ChatCompletionAccumulator

    private var completed: CompletableDeferred<List<ChatCompletionChunk>>? = null

    suspend fun Reactions.sayFinalContent() =
        llm.awaitFinalContent()?.let(::say)

    suspend fun Reactions.streamOrSay() =
        stream?.say(llm.contentStream()) ?: llm.content()?.let(::say)

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

    internal suspend fun LLMContext.startStream() = withActiveJob {
        completed = null
        acc = ChatCompletionAccumulator.Companion.create()
        response = api.createStreaming(params)
        stream = response.stream()
        throwIfCancelled()
    }

    private fun closeStream() {
        if (::response.isInitialized) {
            response.close()
        }
    }

    suspend fun LLMContext.chunkStream(): Stream<ChatCompletionChunk> = withActiveJob {
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

                        val reason = chunk.choices().firstOrNull()?.finishReason()?.getOrNull()
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

    suspend fun LLMContext.chatCompletion(): ChatCompletion {
        return try {
            acc.chatCompletion()
        } catch (e: Exception) {
            chunkStream().count()
            acc.chatCompletion()
        }
    }

    suspend fun LLMContext.choice() = chatCompletion().choices().first()

    suspend fun LLMContext.usage() = chatCompletion().usage()

    suspend fun LLMContext.message() = choice().message()

    suspend fun LLMContext.content() = message().content().getOrNull()

    suspend fun LLMContext.toolCalls() = message().toolCalls().getOrDefault(emptyList())

    suspend fun LLMContext.hasToolCalls() = toolCalls().isNotEmpty()

    suspend fun LLMContext.deltaStream(): Stream<ChatCompletionChunk.Choice.Delta> =
        chunkStream().map { it.choices().first().delta() }

    suspend fun LLMContext.contentStream(): Stream<String> = deltaStream()
        .map { it.content() }
        .filter { it.isPresent }
        .map { it.get() }

    suspend fun LLMContext.eventStream(callTools: Boolean = true): Stream<LLMEvent> = channelStream { channel ->
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

    suspend fun LLMContext.callTools(): List<Result<LLMToolResult>> {
        return coroutineScope {
            toolCalls().map { call ->
                async { runCatching { callTool(call) } }
            }.awaitAll()
        }
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
        ).also { result ->
            BotEngine.current()?.run {
                hooks.triggerHook(
                    LLMToolCallHook(
                        model.states[context.dialogContext.currentState]!!,
                        context, request, reactions, activator, result
                    )
                )
            }
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

        BotEngine.current()?.run {
            hooks.triggerHook(
                LLMToolCallsHook(
                    model.states[context.dialogContext.currentState]!!,
                    context, request, reactions, activator, toolCallResults
                )
            )
        }

        if (toolCallExceptions.isNotEmpty()) {
            throw toolCallExceptions.first()
        }

        startStream()
        return toolCallResults
    }

    suspend fun LLMContext.withToolCalls(
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

    suspend fun LLMContext.awaitFinalMessage() =
        withToolCalls().message()

    suspend fun LLMContext.awaitFinalContent() =
        awaitFinalMessage().content().getOrNull()

    suspend inline fun <reified T> LLMContext.awaitStructuredContent(): T =
        props.responseFormat?.let { format ->
            awaitFinalContent().let { StructuredOutputMapper.readValue(it, format) as T }
        } ?: throw IllegalArgumentException("Response format is not defined in props")

    companion object {
        val StructuredOutputMapper: JsonMapper =
            JsonMapper.builder()
                .addModule(kotlinModule())
                .addModule(Jdk8Module())
                .addModule(JavaTimeModule())
                .build()

    }
}

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