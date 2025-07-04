package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.awaitFinalContent
import com.justai.jaicf.channel.ConsoleChannel
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
val mainAgent = LLMAgent(
    name = "Main",
    props = {
        model = "gpt-4.1-nano"
    }
) {
    println(">> Main agent is thinking...")
    activator.awaitFinalContent()?.also(reactions::say)
}

/**
 * This agent has a calculator tool
 */
val calculatorAgent = LLMAgent(
    name = "Calculator",
    props = {
        model = "gpt-4.1-mini"
        tool(CalcTool)
    }
) {
    // Custom action block for LLM responses processing
    println(">> Calculator agent is thinking...")
    activator.awaitFinalContent()?.also(reactions::say)
}
    .withRole("Agent that makes math calculations well")  // Agent's role describes how other agents determine it


fun main() {
    // Main agent can hand off to calculator agent, while calculator can hand off back if non-math request was received.
    mainAgent.handoff(calculatorAgent)

    // Calculator agent can hand off back to the main agent with a custom role
    calculatorAgent.handoff(mainAgent.withRole("Handoff to this agent if you received a non-math request"))

    // Agent with its handoffs can be exposed as a standalone BotEngine via `asBot`
    ConsoleChannel(mainAgent.asBot).run("2 + 2")
}