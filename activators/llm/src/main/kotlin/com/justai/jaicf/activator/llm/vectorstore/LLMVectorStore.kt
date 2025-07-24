package com.justai.jaicf.activator.llm.vectorstore

import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.llmTool

typealias LLMVectorStoreResponseBuilder = suspend LLMToolCallContext<LLMVectorStore.Request>.(response: LLMVectorStore.Response) -> Any

interface LLMVectorStore {
    suspend fun search(request: Request): Response

    data class Request(val query: String)

    data class Response(val chunks: List<Chunk>) {
        data class Chunk(
            val score: Double,
            val content: String,
            val source: Source? = null,
        )
        data class Source(
            val id: String,
            val name: String? = null,
        )
    }
}

fun LLMVectorStore.asTool(
    name: String,
    description: String = "",
    responseBuilder: LLMVectorStoreResponseBuilder = { it }
) = llmTool<LLMVectorStore.Request>(
    name = "search_${name.replace(" ", "")}",
    description = "Search relevant text chunks in documents of vector store '$name'. $description",
) {
    responseBuilder(search(call.arguments))
}