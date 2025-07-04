package com.justai.jaicf.activator.llm

import com.openai.models.chat.completions.*
import com.openai.models.chat.completions.ChatCompletionContentPartImage.ImageUrl.*

object LLMMessage {
    val userBuilder
        get() = ChatCompletionUserMessageParam.builder()
    val assistantBuilder
        get() = ChatCompletionAssistantMessageParam.builder()
    val systemBuilder
        get() = ChatCompletionSystemMessageParam.builder()
    val toolBuilder
        get() = ChatCompletionToolMessageParam.builder()

    fun user(content: String) = ChatCompletionMessageParam.ofUser(
        userBuilder.content(content).build()
    )

    fun user(content: ChatCompletionUserMessageParam.Content) = ChatCompletionMessageParam.ofUser(
        userBuilder.content(content).build()
    )

    fun user(builder: ChatCompletionUserMessageParam.Builder.() -> Unit) = ChatCompletionMessageParam.ofUser(
        userBuilder.apply(builder).build()
    )

    fun assistant(content: String) = ChatCompletionMessageParam.ofAssistant(
        assistantBuilder.content(content).build()
    )

    fun assistant(content: ChatCompletionAssistantMessageParam.Content) = ChatCompletionMessageParam.ofAssistant(
        assistantBuilder.content(content).build()
    )

    fun system(content: String) = ChatCompletionMessageParam.ofSystem(
        systemBuilder.content(content).build()
    )

    fun system(builder: ChatCompletionSystemMessageParam.Builder.() -> Unit) = ChatCompletionMessageParam.ofSystem(
        systemBuilder.apply(builder).build()
    )

    fun tool(result: LLMToolResult) = ChatCompletionMessageParam.ofTool(
        toolBuilder.toolCallId(result.callId).contentAsJson(result.result).build()
    )

    fun image(url: String, detail: Detail = Detail.AUTO) = user {
        content(ChatCompletionUserMessageParam.Content.ofArrayOfContentParts(
            listOf(
                ChatCompletionContentPart.ofImageUrl(ChatCompletionContentPartImage.builder().imageUrl(
                    ChatCompletionContentPartImage.ImageUrl.builder().url(url).detail(detail).build()
                ).build())
            )
        ))
    }
}
