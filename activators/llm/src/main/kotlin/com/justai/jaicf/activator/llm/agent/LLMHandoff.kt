package com.justai.jaicf.activator.llm.agent

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonTypeName
import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.tool.interrupt
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.botEngine
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.justai.jaicf.logging.GoReaction
import com.justai.jaicf.plugin.PathValue
import com.justai.jaicf.reactions.Reactions
import com.openai.models.chat.completions.ChatCompletionMessageParam
import kotlin.coroutines.coroutineContext


private val HANDOFF_PROMPT_PREFIX = """
    You are part of a multi-agent system, designed to make agent coordination and execution easy. 
    Agents uses two primary abstractions: **Agents** and **Handoffs**. 
    An agent encompasses instructions and tools and can hand off a conversation to another agent when appropriate. 
    Handoffs are achieved by calling a `handoff` function. 
    Transfers between agents are handled seamlessly in the background; do not mention or draw attention to these transfers in your conversation with the user.
""".trimIndent()

@JsonTypeName("handoff_to_agent")
@JsonClassDescription("Handoff conversation to another agent")
data class Handoff(val agent: String)

internal var BotContext.handoffMessages
    by tempProperty<List<ChatCompletionMessageParam>> { emptyList() }

private val LLMAgent.handoffSystemMessage
    get() = HANDOFF_PROMPT_PREFIX +
        handoffs.joinToString("\n", "\nYou can handoff conversation to one of these agents:\n") {
            " - ${it.name}: ${it.role}"
        }

private val LLMAgent.handoffTool
    get() = llmTool<Handoff>(
        parameters = {
            str(
                name = "agent",
                description = "Agent name to handoff conversation to",
                values = handoffs.map { it.name },
                required = true,
            )
        }
    ) {
        val model = coroutineContext.botEngine?.model
        if (model == null) {
            throw IllegalStateException("Scenario model is not available in current context")
        }
        interrupt {
            val state = agentStateName(call.arguments.agent)
            model.states.keys.find { it.endsWith(state) }?.also { path ->
                reactions.handoff(path, llm.params.messages())
            } ?: throw IllegalArgumentException("Agent not found [${call.arguments.agent}]")
        }
    }

internal fun LLMProps.Builder.setupHandoffProps(agent: LLMAgent) {
    agent.handoffs.isNotEmpty().ifTrue {
        tool(agent.handoffTool)
        messages = messages.withSystemMessage("Handoff", agent.handoffSystemMessage)
    }
}

fun Reactions.handoff(
    @PathValue path: String,
    messages: List<ChatCompletionMessageParam>,
): GoReaction {
    botContext.handoffMessages = messages
    return go(path)
}