package com.justai.jaicf.activator.llm.mcp

import com.justai.jaicf.activator.llm.builder.inputSchemaToBuilder
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.llmTool
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.modelcontextprotocol.kotlin.sdk.CallToolResultBase
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlin.time.Duration

typealias McpServiceResponseBuilder = suspend LLMToolCallContext<Map<String, Any>>.(response: Any) -> Any

class McpService() : AutoCloseable {

    private val mcp: Client = Client(clientInfo = Implementation(name = "mcp-client-jaicf", version = "1.0.0"))
    private var process: Process? = null

    private suspend fun connectStdio(command: List<String>) {
        process = runCatching { ProcessBuilder(command).start() }
            .getOrElse { throw IllegalStateException("Failed to start MCP process with command: $command", it) }

        process?.let { proc ->
            val transport = StdioClientTransport(
                input = proc.inputStream.asSource().buffered(),
                output = proc.outputStream.asSink().buffered()
            )
            mcp.connect(transport)
        }
    }

    private suspend fun connectSse(
        urlString: String,
        client: HttpClient,
        reconnectionTime: Duration?,
        requestBuilder: HttpRequestBuilder.() -> Unit
    ) {
        val transport = SseClientTransport(client, urlString, reconnectionTime, requestBuilder)
        mcp.connect(transport)
    }

    private suspend fun connectWebSocket(
        urlString: String,
        client: HttpClient,
        requestBuilder: HttpRequestBuilder.() -> Unit
    ) {
        val transport = WebSocketClientTransport(client, urlString, requestBuilder)
        mcp.connect(transport)
    }

    private suspend fun connectStreamableHttp(
        urlString: String,
        client: HttpClient,
        reconnectionTime: Duration?,
        requestBuilder: HttpRequestBuilder.() -> Unit
    ) {
        val transport = StreamableHttpClientTransport(client, urlString, reconnectionTime, requestBuilder)
        mcp.connect(transport)
    }

    private suspend fun getTools(): List<Tool> = mcp.listTools()?.tools ?: emptyList()

    private fun asTool(
        tool: Tool,
        description: String?,
        responseBuilder: McpServiceResponseBuilder
    ): LLMTool<Map<String, Any>> {
        return llmTool<Map<String, Any>>(
            name = tool.name,
            description = description ?: tool.description ?: "",
            parameters = {
                val inputSchemaMap = mapOf(
                    "type" to tool.inputSchema.type,
                    "properties" to tool.inputSchema.properties,
                    "required" to (tool.inputSchema.required ?: emptyList())
                )
                inputSchemaToBuilder(inputSchemaMap)
            }
        ) {
            responseBuilder(callTool(tool.name, call.arguments))
        }
    }

    fun tools(
        toolNames: List<String> = emptyList(),
        responseBuilder: McpServiceResponseBuilder = { it }
    ): List<LLMTool<Map<String, Any>>> {
        val availableTools = runBlocking { getTools() }
        return availableTools
            .filter { toolNames.isEmpty() || it.name in toolNames }
            .map { asTool(it, null, responseBuilder) }
    }

    fun tool(
        toolName: String,
        description: String? = null,
        responseBuilder: McpServiceResponseBuilder = { it }
    ): LLMTool<Map<String, Any>> {

        val availableTools = runBlocking { getTools() }
        val tool = availableTools.find { it.name == toolName }
            ?: throw IllegalArgumentException("Unknown tool $toolName")

        return asTool(tool, description, responseBuilder)
    }

    suspend fun callTool(name: String, arguments: Map<String, Any?>): CallToolResultBase {
        return mcp.callTool(name, arguments)
            ?: throw IllegalStateException("Error calling tool $name")
    }

    override fun close() {
        runBlocking { mcp.close() }
        process?.destroy()
    }

    companion object {
        fun stdio(
            command: List<String>
        ) = McpService().apply { runBlocking { connectStdio(command) } }

        fun sse(
            url: String,
            client: HttpClient = HttpClient { install(SSE) },
            reconnectionTime: Duration? = null,
            requestBuilder: HttpRequestBuilder.() -> Unit = {}
        ) = McpService().apply { runBlocking { connectSse(url, client, reconnectionTime) { requestBuilder() } } }

        fun websocket(
            urlString: String,
            client: HttpClient = HttpClient { install(WebSockets) },
            requestBuilder: HttpRequestBuilder.() -> Unit = {}
        ) = McpService().apply { runBlocking { connectWebSocket(urlString, client, requestBuilder) } }

        fun streamableHttp(
            urlString: String,
            client: HttpClient = HttpClient(CIO) { install(SSE) },
            reconnectionTime: Duration? = null,
            requestBuilder: HttpRequestBuilder.() -> Unit = {}
        ) = McpService().apply {
            runBlocking {
                connectStreamableHttp(
                    urlString,
                    client,
                    reconnectionTime,
                    requestBuilder
                )
            }
        }
    }
}
