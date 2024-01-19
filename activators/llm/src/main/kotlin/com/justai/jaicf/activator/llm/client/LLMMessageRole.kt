package com.justai.jaicf.activator.llm.client

enum class LLMMessageRole {
    system, user, assistant;

    val isSystem get() = this === system
    val isUser get() = this === user
    val isAssistant get() = this === assistant
}