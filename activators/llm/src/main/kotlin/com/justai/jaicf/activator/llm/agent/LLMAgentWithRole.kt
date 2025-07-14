package com.justai.jaicf.activator.llm.agent

class LLMAgentWithRole(
    agent: LLMAgent,
    val role: String,
) : LLMAgent(agent)

fun LLMAgent.withRole(role: String) = LLMAgentWithRole(this, role)