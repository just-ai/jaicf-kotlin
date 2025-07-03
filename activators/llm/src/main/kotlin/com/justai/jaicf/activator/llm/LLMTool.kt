package com.justai.jaicf.activator.llm

typealias LLMToolFunction<T> = LLMActivatorContext.(arguments: T) -> Any

data class LLMTool<T>(
    val definition: Class<T>,
    val function: LLMToolFunction<T>,
)

inline fun <reified T> llmTool(
    noinline function: LLMToolFunction<T>
) = LLMTool(T::class.java, function)

data class LLMToolResult(
    val callId: String,
    val name: String,
    val arguments: Any,
    val result: Any,
) {
    inline fun <reified T> arguments() = arguments as T
    inline fun <reified T> result() = result as T
}