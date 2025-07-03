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
 */

/**
 * This agent has a calculator tool
 */
val calculatorAgent = LLMAgent(
    name = "calculator",                                // Name used for state definition
    role = "Agent that makes math calculations well",   // Role describes how other agents determine this agent
    props = {
        model = "gpt-4.1-mini"
        tool(CalcTool)
    }
) {
    // Custom action block for LLM responses processing
    println(">> Calculator agent is thinking...")
    activator.awaitFinalContent()?.also(reactions::say)
}

/**
 * The main agent that doesn't have any tools and works on the fastest LLM
 */
val mainAgent = LLMAgent(
    name = "main",
    role = "Main agent that dispatches to other agents",
    props = {
        model = "gpt-4.1-nano"
    }
) {
    println(">> Main agent is thinking...")
    activator.awaitFinalContent()?.also(reactions::say)
}

fun main() {
    // Main agent can hand off to calculator agent, while calculator can hand off back if non-math request was received.
    mainAgent.handoff(
    calculatorAgent.handoff(
        mainAgent.asHandoff("Handoff to this agent any non-math request")  // Define a different role for better routing
    ))

    ConsoleChannel(mainAgent.asBot).run("2 + 2")
}