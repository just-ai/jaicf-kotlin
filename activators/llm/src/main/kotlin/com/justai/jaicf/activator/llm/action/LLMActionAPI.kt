package com.justai.jaicf.activator.llm.action

import com.justai.jaicf.activator.llm.*
import com.justai.jaicf.activator.llm.agent.handoffMessages
import com.justai.jaicf.activator.llm.builder.build
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.http.StreamResponse
import com.openai.models.chat.completions.ChatCompletionChunk
import com.openai.models.chat.completions.ChatCompletionCreateParams


private val DefaultOpenAIClient = OpenAIOkHttpClient.fromEnv()
private val DefaultProps = LLMProps(
    client = DefaultOpenAIClient,
    withUsages = true,
)

class LLMActionAPI(val defaultProps: LLMProps = DefaultProps) {
    fun createStreaming(
        params: ChatCompletionCreateParams,
        client: OpenAIClient? = null,
    ): StreamResponse<ChatCompletionChunk> {
        val client = client ?: defaultProps.client ?: DefaultOpenAIClient
        return client.chat().completions().createStreaming(params)
    }

    internal fun createContext(
        context: BotContext,
        request: BotRequest,
        props: LLMPropsBuilder = DefaultLLMProps,
    ): LLMContext {
        val props = defaultProps.withOptions(props.build(context, request))
        val params = props.toChatCompletionCreateParams().apply {
            if (context.handoffMessages.isEmpty()) {
                val builder = props.input ?: LLMInputs.TextOnly
                builder.invoke(request)?.forEach(::addMessage)
                    ?: throw IllegalArgumentException("Request is not supported: $request")
            } else {
                messages(props.messages.orEmpty()
                    .filter { it.isSystem() || it.isDeveloper() } +
                    context.handoffMessages.filter { !it.isSystem() && !it.isDeveloper() }
                )
                context.handoffMessages = emptyList()
            }
        }.build(props)

        return LLMContext(this, context, request, params, props)
    }

    companion object {
        private lateinit var instance: LLMActionAPI

        fun init(defaultProps: LLMProps = DefaultProps): LLMActionAPI {
            if (::instance.isInitialized) {
                throw IllegalStateException("LLMActivatorAPI is initialized already")
            }
            instance = LLMActionAPI(defaultProps)
            return instance
        }

        val get: LLMActionAPI
            get() {
               if (!::instance.isInitialized) { init() }
               return instance
            }
    }
}
