package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.action.LLMActionAPI
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.BotContext
import com.openai.models.chat.completions.ChatCompletionCreateParams


class LLMContext(
    val api: LLMActionAPI,
    internal val context: BotContext,
    internal val request: BotRequest,
    internal var params: ChatCompletionCreateParams,
    val props: LLMProps,
) {
    val chatCompletionParams
        get() = params
}