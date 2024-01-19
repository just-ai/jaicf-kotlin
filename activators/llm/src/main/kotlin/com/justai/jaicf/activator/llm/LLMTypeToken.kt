package com.justai.jaicf.activator.llm

import com.justai.jaicf.generic.ActivatorTypeToken

typealias LLMTypeToken = ActivatorTypeToken<LLMActivatorContext>
typealias LLMMessageTypeToken = ActivatorTypeToken<LLMMessageActivatorContext>
typealias LLMFunctionTypeToken = ActivatorTypeToken<LLMFunctionActivatorContext>

val llm: LLMTypeToken = ActivatorTypeToken()
val llmMessage: LLMMessageTypeToken = ActivatorTypeToken()
val llmFunction: LLMFunctionTypeToken = ActivatorTypeToken()