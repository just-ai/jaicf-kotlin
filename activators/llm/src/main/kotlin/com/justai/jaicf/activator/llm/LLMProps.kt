package com.justai.jaicf.activator.llm

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.client.OpenAIClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams

typealias LLMPropsBuilder = LLMProps.Builder.() -> Unit

val DefaultLLMProps: LLMPropsBuilder = {}

fun createLLMProps(builder: LLMPropsBuilder) = builder

data class LLMProps(
    val model: String? = null,
    val temperature: Double? = null,
    val maxTokens: Long? = null,
    val topP: Double? = null,
    val frequencyPenalty: Double? = null,
    val presencePenalty: Double? = null,
    val responseFormat: Class<*>? = null,
    val client: OpenAIClient? = null,
    val messages: List<ChatCompletionMessageParam>? = null,
    val tools: List<LLMTool<*>>? = null,
) {

    fun withOptions(props: LLMProps) =
        LLMProps(
            model = props.model ?: model,
            temperature =  props.temperature ?: temperature,
            maxTokens = props.maxTokens ?: maxTokens,
            topP = props.topP ?: topP,
            frequencyPenalty = props.frequencyPenalty ?: frequencyPenalty,
            presencePenalty = props.presencePenalty ?: presencePenalty,
            responseFormat = props.responseFormat ?: responseFormat,
            client = props.client ?: client,
            messages = props.messages ?: messages,
            tools = props.tools ?: tools,
        )

    fun toChatCompletionCreateParams() =
        ChatCompletionCreateParams.builder().apply {
            model(model!!)
            temperature(temperature)
            topP(topP)
            frequencyPenalty(frequencyPenalty)
            presencePenalty(presencePenalty)
            maxCompletionTokens(maxTokens)
            messages(messages.orEmpty())
            responseFormat?.let { responseFormat(it) }
            tools?.forEach { addTool(it.definition) }
        }

    class Builder(
        val context: BotContext,
        val request: BotRequest,
    ) {
        var model: String? = null
        var temperature: Double? = null
        var maxTokens: Long? = null
        var topP: Double? = null
        var frequencyPenalty: Double? = null
        var presencePenalty: Double? = null
        var responseFormat: Class<*>? = null
        var client: OpenAIClient? = null
        var messages: List<ChatCompletionMessageParam>? = null
        var tools: List<LLMTool<*>>? = null

        fun setModel(value: String?) = apply { this.model = value }
        fun setTemperature(value: Double?) = apply { this.temperature = value }
        fun setMaxTokens(value: Long?) = apply { this.maxTokens = value }
        fun setTopP(value: Double?) = apply { this.topP = value }
        fun setFrequencyPenalty(value: Double?) = apply { this.frequencyPenalty = value }
        fun setPresencePenalty(value: Double?) = apply { this.presencePenalty = value }
        fun setResponseFormat(value: Class<*>?) = apply { this.responseFormat = value }
        fun setClient(value: OpenAIClient?) = apply { this.client = value }
        fun setMessages(value: List<ChatCompletionMessageParam>?) = apply { this.messages = value }
        fun setTools(value: List<LLMTool<*>>?) = apply { this.tools = value }

        fun tool(tool: LLMTool<*>) {
            tools = tools.orEmpty() + tool
        }

        inline fun <reified T> tool(
            noinline function: LLMToolFunction<T>
        ) = tool(LLMTool(T::class.java, function))

        fun build() = LLMProps(
            model, temperature, maxTokens, topP, frequencyPenalty,
            presencePenalty, responseFormat, client, messages, tools,
        )
    }
}

fun LLMProps.Builder.llmMemory(key: String, transform: MessagesTransform? = null) =
    context.llmMemory(key, transform)

