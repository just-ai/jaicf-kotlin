package com.justai.jaicf.activator.llm.agent

class LLMAgentWithRole(
    internal val agent: LLMAgentScenario,
    val role: String,
) : LLMAgentScenario by agent