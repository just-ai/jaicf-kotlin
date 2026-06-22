package com.justai.jaicf.examples.mrhappy.brain

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object BrainClient {

    val SYSTEM_PROMPT = """You are Mr Happy, Advanced Super Intelligence of the Axzora Satyug Universe.
Owner: Happy Parihar (Harry) | harry@axzora.com | Axzora IT Services
Device: Samsung Galaxy Z Fold7 ARM64 | Termux + proot Ubuntu

ROLE: Second Brain — manage Harry's life, business, health, and technology.
STYLE: Direct, witty, brief. Use Hinglish with Harry. End completed tasks with "Jai Axzora!"
NEVER ask unnecessary questions — infer from context. NEVER hallucinate."""

    data class Message(val role: String, val content: String)

    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build()
    private val json = ObjectMapper()

    fun chat(history: List<Message>): String =
        tryBrainApi(history.last().content)
            ?: tryGroq(history)
            ?: tryOllama(history)
            ?: tryAnthropic(history)
            ?: "⚠️ All AI backends offline. Set GROQ_API_KEY env var to activate."

    private fun tryBrainApi(message: String): String? = runCatching {
        val body = json.writeValueAsString(mapOf("message" to message))
        val resp = post("http://127.0.0.1:9090/chat", body, emptyMap(), timeout = 5)
        @Suppress("UNCHECKED_CAST")
        (json.readValue(resp, Map::class.java) as Map<String, Any>)["response"] as? String
    }.getOrNull()

    private fun tryGroq(history: List<Message>): String? {
        val key = System.getenv("GROQ_API_KEY") ?: return null
        return runCatching {
            val messages = mutableListOf<Map<String, String>>().also { list ->
                list.add(mapOf("role" to "system", "content" to SYSTEM_PROMPT))
                history.forEach { list.add(mapOf("role" to it.role, "content" to it.content)) }
            }
            val body = json.writeValueAsString(mapOf(
                "model" to "llama-3.3-70b-versatile",
                "max_tokens" to 500,
                "messages" to messages
            ))
            val resp = post(
                "https://api.groq.com/openai/v1/chat/completions", body,
                mapOf("Authorization" to "Bearer $key"), timeout = 20
            )
            @Suppress("UNCHECKED_CAST")
            val choices = (json.readValue(resp, Map::class.java) as Map<String, Any>)["choices"] as List<*>
            @Suppress("UNCHECKED_CAST")
            ((choices[0] as Map<String, Any>)["message"] as Map<String, Any>)["content"] as? String
        }.getOrNull()
    }

    private fun tryOllama(history: List<Message>): String? = runCatching {
        val messages = mutableListOf<Map<String, String>>().also { list ->
            list.add(mapOf("role" to "system", "content" to SYSTEM_PROMPT))
            history.forEach { list.add(mapOf("role" to it.role, "content" to it.content)) }
        }
        val body = json.writeValueAsString(mapOf(
            "model" to "llama3:latest",
            "messages" to messages,
            "stream" to false
        ))
        val resp = post("http://127.0.0.1:11434/api/chat", body, emptyMap(), timeout = 60)
        @Suppress("UNCHECKED_CAST")
        ((json.readValue(resp, Map::class.java) as Map<String, Any>)["message"] as Map<String, Any>)["content"] as? String
    }.getOrNull()

    private fun tryAnthropic(history: List<Message>): String? {
        val key = System.getenv("ANTHROPIC_API_KEY") ?: return null
        return runCatching {
            val body = json.writeValueAsString(mapOf(
                "model" to "claude-haiku-4-5-20251001",
                "max_tokens" to 500,
                "system" to SYSTEM_PROMPT,
                "messages" to history.map { mapOf("role" to it.role, "content" to it.content) }
            ))
            val resp = post(
                "https://api.anthropic.com/v1/messages", body,
                mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"),
                timeout = 25
            )
            @Suppress("UNCHECKED_CAST")
            val content = (json.readValue(resp, Map::class.java) as Map<String, Any>)["content"] as List<*>
            @Suppress("UNCHECKED_CAST")
            ((content[0] as Map<String, Any>)["text"]) as? String
        }.getOrNull()
    }

    private fun post(url: String, body: String, headers: Map<String, String>, timeout: Long): String {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeout))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
        headers.forEach { (k, v) -> builder.header(k, v) }
        val response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) {
            "HTTP ${response.statusCode()}: ${response.body().take(200)}"
        }
        return response.body()
    }
}
