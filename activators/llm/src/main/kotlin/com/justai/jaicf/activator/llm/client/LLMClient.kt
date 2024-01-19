package com.justai.jaicf.activator.llm.client

interface LLMClient {
    fun chatCompletion(request: LLMRequest): LLMResponse
}
