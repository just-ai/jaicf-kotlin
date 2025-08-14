package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.llm.vectorstore.OpenAIVectorStoreFactory
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.helpers.context.tempProperty
import com.openai.client.okhttp.OpenAIOkHttpClient
import java.io.File

/**
 * This example shows how to use vector stores.
 * Each vector store is attached to LLM scenario or agent as a tool with custom description and returns a list of query-relevant chunks.
 * Description defines when to use this vector store to search relevant chunks.
 */

private val client = OpenAIOkHttpClient.builder().build()

/**
 * Store can be created on the fly or defined statically.
 * This one creates a new OpenAI vector store and uploads JAICF documentation to it.
 */
private val store = OpenAIVectorStoreFactory(client).create("JAICF").apply {
    File("./docs/pages").walkTopDown()
        .filter { it.isFile && it.extension == "md" }
        .forEach { uploadFile(it.toPath()) }
}

private var BotContext.sources by tempProperty { listOf<String?>() }

private val scenario = Scenario {
    llmState("main", {
        model = "gpt-4.1-nano"

        vectorStore(store, "jaicf", "This store contains full documentation of JAICF framework") {
            // Custom builder can be used to re-build vector store response if needed.
            // This builder just adds sources to the temp context.
            it.also { context.sources = it.chunks.map { c -> c.source?.name } }
        }
    }) {
        llm.withToolCalls {
            reactions.streamOrSay()
        }

        // Append a list of sources to the response from temp context filled with custom builder above.
        context.sources.filter { !it.isNullOrEmpty() }.takeIf { it.isNotEmpty() }?.also { sources ->
            reactions.say(sources.joinToString(prefix = "SOURCES: "))
        }
    }
}

fun main() {
    ConsoleChannel(BotEngine(scenario))
        .run("How to create scenario in JAICF?")
}