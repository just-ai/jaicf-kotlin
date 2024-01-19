package com.justai.jaicf.examples.llm

data class LLMBotState(
    val interests: List<String>? = null,
    val adults: Int? = null,
    val kids: Int? = null,
    val date: String? = null,
    val place: String? = null,
    val suggestions: List<String>? = null,
)