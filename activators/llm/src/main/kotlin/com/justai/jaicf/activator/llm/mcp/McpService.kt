package com.justai.jaicf.activator.llm.mcp

import com.justai.jaicf.activator.llm.builder.JsonSchemaBuilder
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.llmTool
import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.modelcontextprotocol.kotlin.sdk.CallToolResultBase
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.WebSocketClientTransport
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlin.time.Duration

typealias McpServiceResponseBuilder = suspend LLMToolCallContext<Map<String, Any>>.(response: Any) -> Any

class McpServiceImpl() : AutoCloseable {

    private val mcp: Client = Client(clientInfo = Implementation(name = "mcp-client-jaicf", version = "1.0.0"))

    suspend fun connectStdio(command: List<String>) {
        val process = ProcessBuilder(command).start()

        val transport = StdioClientTransport(
            input = process.inputStream.asSource().buffered(),
            output = process.outputStream.asSink().buffered()
        )

        mcp.connect(transport)
    }

    suspend fun connectSse(
        urlString: String,
        client: HttpClient,
        reconnectionTime: Duration?,
        requestBuilder: HttpRequestBuilder.() -> Unit
    ) {
        val transport = SseClientTransport(client, urlString, reconnectionTime, requestBuilder)
        mcp.connect(transport)
    }

    suspend fun connectWebSocket(
        urlString: String,
        client: HttpClient,
        requestBuilder: HttpRequestBuilder.() -> Unit
    ) {
        val transport = WebSocketClientTransport(client, urlString, requestBuilder)
        mcp.connect(transport)
    }

    suspend fun getTools(): List<Tool> = mcp.listTools()?.tools ?: emptyList()

    suspend fun callTool(name: String, arguments: Map<String, Any?>): CallToolResultBase {
        return mcp.callTool(name, arguments)
            ?: throw IllegalStateException("Error calling tool $name")
    }

    override fun close() {
        runBlocking { mcp.close() }
    }
}

class McpServiceImplFactory {
    fun stdio(
        command: List<String>
    ) = McpServiceImpl().apply { runBlocking { connectStdio(command) } }

    fun sse(
        urlString: String,
        client: HttpClient = HttpClient { install(SSE) },
        reconnectionTime: Duration? = null,
        requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ) = McpServiceImpl().apply { runBlocking { connectSse(urlString, client, reconnectionTime) { requestBuilder() } } }

    fun websocket(
        urlString: String,
        client: HttpClient = HttpClient { install(WebSockets) },
        requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ) = McpServiceImpl().apply { runBlocking { connectWebSocket(urlString, client, requestBuilder) } }
}

fun McpServiceImpl.asTools(
    tools: List<String> = emptyList(),
    responseBuilder: McpServiceResponseBuilder = { it }
): List<LLMTool<Map<String, Any>>> {
    val availableTools = runBlocking { getTools() }
    return availableTools
        .filter { tools.isEmpty() || it.name in tools }
        .map { getLlmTool(it, null, responseBuilder) }
}

fun McpServiceImpl.getTool(
    toolName: String,
    description: String?,
    responseBuilder: McpServiceResponseBuilder = { it }
): LLMTool<Map<String, Any>> {

    val availableTools = runBlocking { getTools() }
    val tool = availableTools.find { it.name == toolName }
        ?: throw IllegalArgumentException("Unknown tool $toolName")

    return getLlmTool(tool, description, responseBuilder)
}

private fun McpServiceImpl.getLlmTool(
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
            this.convertInputSchemaToBuilder(inputSchemaMap)
        }
    ) {
        responseBuilder(callTool(tool.name, call.arguments))
    }
}

private fun JsonSchemaBuilder.convertInputSchemaToBuilder(inputSchema: Map<String, Any>?) {
    inputSchema ?: return

    val properties = inputSchema["properties"] as? Map<String, Any> ?: return
    val required = inputSchema["required"] as? List<String> ?: emptyList()

    properties.forEach { (propName, propDef) ->
        val propMap = when (propDef) {
            is kotlinx.serialization.json.JsonObject -> {
                propDef.entries.associate { (key, value) ->
                    key to when (value) {
                        is kotlinx.serialization.json.JsonPrimitive -> {
                            when {
                                value.isString -> value.content
                                value.content == "true" || value.content == "false" -> value.content.toBoolean()
                                value.content.toIntOrNull() != null -> value.content.toInt()
                                value.content.toDoubleOrNull() != null -> value.content.toDouble()
                                else -> value.content
                            }
                        }
                        is kotlinx.serialization.json.JsonArray -> {
                            value.map {
                                if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString()
                            }
                        }
                        else -> value.toString()
                    }
                }
            }
            is Map<*, *> -> propDef as? Map<String, Any>
            else -> null
        } ?: return@forEach

        val type = propMap["type"] as? String ?: return@forEach
        val description = propMap["description"] as? String
        val isRequired = propName in required

        when (type) {
            "string" -> {
                val enumValues = (propMap["enum"] as? List<*>)?.mapNotNull { it as? String }
                str(propName, description, isRequired, enumValues)
            }
            "integer" -> {
                int(propName, description, isRequired)
            }
            "number" -> {
                num(propName, description, isRequired)
            }
            "boolean" -> {
                bool(propName, description, isRequired)
            }
        }
    }
}