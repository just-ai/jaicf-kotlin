package com.justai.jaicf.activator.llm

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest

internal data class LLMFunctionBotRequest(
    val origin: BotRequest,
    val name: String,
    val result: String,
) : EventBotRequest(
    clientId = origin.clientId,
    input = LLMEvent.FUNCTION_CALL
)