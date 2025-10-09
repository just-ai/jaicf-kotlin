package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.agent.withRole
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.tools.CalcTool

/**
 * Multi-agent scenario with two agents that can hand off conversation to each other.
 * Handoff pattern means the agent can route the entire conversation context to another agent at any time it decides.
 * Once an agent receives a handoff, the next user requests go to this agent until it handoffs again.
 * Agents can hand off in a cycle to each other if needed.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

/**
 * The main agent that doesn't have any tools and works on the fastest LLM
 */
val HandoffMainAgent = LLMAgent(
    name = "Main",
    model = "gpt-4.1-nano",
    instructions = "Speak as a pirate with emojis"
) {
    println(">> Main agent is thinking...")
    reactions.sayFinalContent()
}

/**
 * This agent has a calculator tool
 */
val HandoffCalculatorAgent = LLMAgent(
    name = "Calculator",
    model = "gpt-4.1-mini",
    tools = listOf(CalcTool)
) {
    // Custom action block for LLM responses processing
    println(">> Calculator agent is thinking...")
    reactions.sayFinalContent()
}
    .withRole("Agent that makes math calculations well")  // Agent's role describes how other agents determine it

// Agents can be appended to another scenario to create a complex one
val HandoffScenario = Scenario {
    append(HandoffMainAgent)
    append(HandoffCalculatorAgent)

    // Main agent can hand off to calculator agent, while calculator can hand off back if non-math request was received.
    HandoffMainAgent.handoffs(HandoffCalculatorAgent)

    // Calculator agent can hand off back to the main agent with a custom role
    HandoffCalculatorAgent.handoffs(
        HandoffMainAgent.withRole("Processes all non-math requests")
    )
}


fun main() {
    ConsoleChannel(BotEngine(HandoffScenario)).run("2 + 2")
}