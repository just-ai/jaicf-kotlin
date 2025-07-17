package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.agent.withRole
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool

/**
 * Multi-agent bot with two agents that can hand off conversation to each other.
 * Handoff pattern means the agent can route the entire conversation context to another agent at any time it decides.
 * Once an agent receives a handoff, the next user requests go to this agent until it handoffs again.
 * Agents can hand off in a cycle to each other if needed.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

/**
 * The main agent that doesn't have any tools and works on the fastest LLM
 */
private val mainAgent = LLMAgent(
    name = "Main",
    model = "gpt-4.1-nano",
    instructions = "Speak as a pirate with emojis"
) {
    println(">> Main agent is thinking...")
    activator.awaitFinalContent()?.also(reactions::say)
}

/**
 * This agent has a calculator tool
 */
private val calculatorAgent = LLMAgent(
    name = "Calculator",
    model = "gpt-4.1-mini",
    tools = listOf(CalcTool)
) {
    // Custom action block for LLM responses processing
    println(">> Calculator agent is thinking...")
    activator.awaitFinalContent()?.also(reactions::say)
}
    .withRole("Agent that makes math calculations well")  // Agent's role describes how other agents determine it


fun main() {
    // Main agent can hand off to calculator agent, while calculator can hand off back if non-math request was received.
    mainAgent.handoffs(calculatorAgent)

    // Calculator agent can hand off back to the main agent with a custom role
    calculatorAgent.handoffs(mainAgent.withRole("Processes all non-math requests"))

    // Agent with its handoffs can be exposed as a standalone BotEngine via `asBot`
    ConsoleChannel(mainAgent.asBot).run("2 + 2")
}